package com.snug.core;

import com.snug.constant.Constants;
import com.snug.storer.DubboStorer;
import com.snug.storer.FieldDocStorer;
import com.snug.storer.MethodStorer;
import com.snug.util.SnugUtil;
import com.sun.javadoc.*;
import com.sun.tools.javadoc.FieldDocImpl;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLOutput;
import java.util.*;

/**
 * SnugDoclet的默认实现
 * 默认是仅仅解析Dubbo接口，如果后面要解析Controller，再写一个SnugDoclet的实现类即可。
 * @Author Abner
 */
@Slf4j
public class DefaultSnugDocletImpl extends SnugDoclet {

    public DefaultSnugDocletImpl(SnugDocContext snugDocContext){
        super(snugDocContext);
        setDoclet(this);//将子类实例挂载到父类静态变量上
    }

    @Override
    protected boolean process(RootDoc root) {
        //处理root
        handleClassDocs(root);
        return true;
    }

    /**
     * 处理所有类
     * @Author Abner
     * @param root
     */
    private void handleClassDocs(RootDoc root) {
        ClassDoc[] classes = root.classes();
        for (ClassDoc classDoc : classes) {
            handleClassDoc(classDoc);
        }
    }

    /**
     * 处理单个类
     *
     * @param classDoc
     */
    private void handleClassDoc(ClassDoc classDoc) {
        AnnotationDesc[] annotationDescs = classDoc.annotations();
        for(AnnotationDesc anno : annotationDescs){
            //只解析dubbo接口的实现类
            if(anno.annotationType().qualifiedName().equals(Constants.DUBBO_SERVICE_ANNOTATION)){
                log.info("找到了Dubbo接口："+classDoc);
                DubboStorer dubboStorer = new DubboStorer();
                dubboStorer.setClassdoc(classDoc);
                dubboStorer.setName(classDoc.qualifiedTypeName());
                //获取类上的注释
                dubboStorer.setComment(classDoc.commentText());
                //获取dubbo所实现的接口
                dubboStorer.setImplementInterDocs(Arrays.asList(classDoc.interfaces()));
                AnnotationDesc.ElementValuePair[] eleVals = anno.elementValues();
                Map<String,String> annoMap = new HashMap<String,String>();
                for(AnnotationDesc.ElementValuePair evp : eleVals){
                    //获取@Service的相关信息，如group，timeout等。
                    annoMap.put(evp.element().name(),evp.value().value().toString());
                }
                dubboStorer.setDubboAnnotationTags(annoMap);
                //获取类上的标签
                Map<String,String> classTags = new HashMap<String, String>();
                for(Tag tag : classDoc.tags()){
                    classTags.put(tag.name().substring(1,tag.name().length()),tag.text());
                }
                dubboStorer.setDubboClassTags(classTags);
                //获取类里对应的方法。
                dubboStorer.setMethods(handleMethods(classDoc));
                System.out.println(dubboStorer.toString());
                //将所有的dubboStorer存储到dubboClasses中
                snugDocContext.dubboClasses.add(dubboStorer);
            }
        }
    }

    /**
     * 处理dubbo的所有方法。实际上是处理接口中的方法。如果接口中的方法没有注释，就到实现类中找注释。
     * 考虑情况：一个实现类没有实现任何接口。
     *          实现类实现了多个接口，但只有一个Service接口。
     *          实现类实现了多个Service接口,但是dubbo只认第一个，后面的无效。
     *
     * @param classDoc
     * @return
     */
    public List<MethodStorer> handleMethods(ClassDoc classDoc){
        List<MethodStorer> methodStorerList = new ArrayList<MethodStorer>();
        //从实现的接口判断接口中的方法
        ClassDoc[] interfaces = classDoc.interfaces();
        if(interfaces.length<1){
            log.error("类 "+classDoc+" 未实现任何接口");
        }
        boolean singleService = false;
        for(ClassDoc interdoc : interfaces){
            //如果只实现了一个接口
            if(!singleService){
                //解析相关接口的方法。
                for(MethodDoc methodDoc : interdoc.methods()){
                    MethodStorer methodStorer = new MethodStorer();
                    methodStorer.setName(methodDoc.name());
                    methodStorer.setClassDoc(classDoc); //实现类的classdoc
                    //这个methodDoc我应该保存实现类的还是接口的呢？
                    //还是解析接口上的方法吧，然后通过classDoc找到实现类上的东西。
                    methodStorer.setMethodDoc(methodDoc);
                    methodStorer.setComment(methodDoc.commentText());
                    Map<String,String> methodTags = new HashMap<String, String>();
                    for(Tag tag : methodDoc.tags()){
                        methodTags.put(tag.name(),tag.text());
                    }
                    methodStorer.setTags(methodTags);

                    //方法参数解析
                    Map<String,FieldDocStorer> parasMap = new HashMap<String, FieldDocStorer>();
                    for(Parameter parameter : methodDoc.parameters()){
                        parasMap.put(parameter.name(),handleParamDoc(parameter));
                    }
                    methodStorer.setParaMap(parasMap);

                    //返回类型解析
                    methodStorer.setReturnType(handleType(methodDoc.returnType()));

                    methodStorerList.add(methodStorer);
                }

                singleService = true;
            }else{
                log.error("类 "+classDoc+" 实现了多个接口");
            }
        }

        return  methodStorerList;
    }

    /**
     *  考虑情况：
     *          基本类型，包装类，String等。
     *          数组，集合，map
     *          自定义泛型类怎么处理。解析自定义类和泛型类？
     * @param parameter
     * @return
     */
    public FieldDocStorer handleParamDoc(Parameter parameter){
        FieldDocStorer fieldDocStorer = handleType(parameter.type());
        fieldDocStorer.setName(parameter.name());

        return fieldDocStorer;
    }

    public FieldDocStorer handleType(Type type){
        FieldDocStorer fieldDocStorer = new FieldDocStorer();
        fieldDocStorer.setType(type.toString());

        Set<String> typeSet = new HashSet<String>();
        typeSet.add(type.toString());

        //如果参数类型是实体类型
        if(SnugUtil.isEntity(type)){
            ClassDoc classDoc = type.asClassDoc();
            fieldDocStorer.setEntity(true);
            //TODO 如果是实体数组呢
            if(SnugUtil.isArray(type)){
                fieldDocStorer.setArray(true);
            }
            //如果参数是实体，获取实体类上的注释。
            fieldDocStorer.setComment(classDoc.commentText());
            //TODO 处理自定义泛型类
            HashMap<String,Type> genericTypeMap = null;   //前面的参数表示泛型，后面的参数表示实际类型
            try{
                Type[] actualTypes = type.asParameterizedType().typeArguments();
                if(actualTypes!=null && actualTypes.length>0){
                    fieldDocStorer.setGeneric(true);
                    Type[] formalTypes = type.asClassDoc().typeParameters();
                    genericTypeMap = new HashMap<>();
                    for(int i=0;i<actualTypes.length;i++){
                        genericTypeMap.put(formalTypes[i].qualifiedTypeName(),actualTypes[i]);
                    }
                }

            }catch (Exception e){

            }

            List<FieldDocStorer> list = new ArrayList<FieldDocStorer>();
            for(FieldDoc fieldDoc : classDoc.fields(false)){
                FieldDocStorer fieldDocStorer1 = null;
                if(genericTypeMap!=null && genericTypeMap.size()>0){
                    if(genericTypeMap.containsKey(fieldDoc.type().qualifiedTypeName())){
                        Type actualType = genericTypeMap.get(fieldDoc.type().qualifiedTypeName());
                        //TODO 将泛型类型替换为实际的类型
                        fieldDocStorer1 = handleFieldDocImprove(fieldDoc,typeSet,actualType);
                    }
                }else{
                    fieldDocStorer1 = handleFieldDocImprove(fieldDoc,typeSet);
                }
                if(fieldDocStorer1!=null){
                    list.add(fieldDocStorer1);
                }

            }
            fieldDocStorer.setTypeArgs(list);

            //将fieldDocStorer添加到model中,去掉数组类型。
            String fieldDocStorerType = fieldDocStorer.getType();
            if(fieldDocStorerType.contains("[]")){
                fieldDocStorerType = fieldDocStorerType.replace("[","").replace("]","");
            }
            if(!snugDocContext.models.containsKey(fieldDocStorerType)){
                snugDocContext.models.put(fieldDocStorerType,fieldDocStorer);
            }
        }else{
            if(SnugUtil.isArray(type)){
                fieldDocStorer.setArray(true);
            }else if(SnugUtil.isCollection(type)){
                fieldDocStorer.setCollection(true);
                //解析集合的泛型
                Type[] subtypes = type.asParameterizedType().typeArguments();
                if(subtypes!=null && subtypes.length>0){
                    if(SnugUtil.isEntity(subtypes[0])){
                        //处理Collection中的泛型
                        List<FieldDocStorer> list = new ArrayList<FieldDocStorer>();
                        ClassDoc classDoc = subtypes[0].asClassDoc();
                        for(FieldDoc fieldDoc1 : classDoc.fields(false)){
                            //如果entity中的一个字段又是collection怎么办？递归之后会被处理的
                            list.add(handleFieldDocImprove(fieldDoc1,typeSet));
                        }
                        fieldDocStorer.setTypeArgs(list);
                    }
                }

            }else if(SnugUtil.isMap(type)){
                fieldDocStorer.setMap(true);
                Type[] mapTypes = type.asParameterizedType().typeArguments();
                if(mapTypes!=null && mapTypes.length>0){
                    for(Type mapType : mapTypes){
                        if(SnugUtil.isEntity(mapType)){
                            //处理Map中的泛型
                            List<FieldDocStorer> list = new ArrayList<FieldDocStorer>();
                            ClassDoc classDoc = mapType.asClassDoc();
                            for(FieldDoc fieldDoc1 : classDoc.fields(false)){
                                list.add(handleFieldDocImprove(fieldDoc1,typeSet));
                            }
                            fieldDocStorer.setTypeArgs(list);
                        }
                    }
                }
            }else{
                fieldDocStorer.setBasic(true);
            }
        }
        return fieldDocStorer;
    }

    public FieldDocStorer handleFieldDocImprove(FieldDoc fieldDoc,Set<String> typeSet,Type actualType){
        if(fieldDoc==null){
            return null;
        }
        FieldDocStorer fieldDocStorer = new FieldDocStorer();
        fieldDocStorer.setName(fieldDoc.name());
        if(actualType!=null){
            fieldDocStorer.setType(actualType.toString());
        }else{
            fieldDocStorer.setType(fieldDoc.type().toString());
        }
        fieldDocStorer.setComment(fieldDoc.commentText());

        if(typeSet.contains(fieldDocStorer.getType())){
            return fieldDocStorer;
        }else{
            typeSet.add(fieldDocStorer.getType());
        }
        Set<String> new_typeSet = new HashSet<String>();
        for(String str : typeSet){
            new_typeSet.add(str);
        }
        Type type = actualType==null ? fieldDoc.type() : actualType;
        if(SnugUtil.isEntity(type)){
            fieldDocStorer.setEntity(true);
            if(SnugUtil.isArray(type)){
                fieldDocStorer.setArray(true);
            }
            List<FieldDocStorer> list = new ArrayList<>();
            ClassDoc classDoc = type.asClassDoc();
            for(FieldDoc fieldDoc1 : classDoc.fields(false)){
                list.add(handleFieldDocImprove(fieldDoc1,new_typeSet));
            }
            fieldDocStorer.setTypeArgs(list);

            //将fieldDocStorer添加到model中,去掉数组类型。
            String fieldDocStorerType = fieldDocStorer.getType();
            if(fieldDocStorerType.contains("[]")){
                fieldDocStorerType = fieldDocStorerType.replace("[","").replace("]","");
            }
            if(!snugDocContext.models.containsKey(fieldDocStorerType)){
                snugDocContext.models.put(fieldDocStorerType,fieldDocStorer);
            }

        }else{
            if(SnugUtil.isArray(type)){
                fieldDocStorer.setArray(true);
            }else if(SnugUtil.isCollection(type)){
                fieldDocStorer.setCollection(true);
                Type[] collectionTypes = type.asParameterizedType().typeArguments();
                if(collectionTypes!=null && collectionTypes.length>0){
                    if(SnugUtil.isEntity(collectionTypes[0])){
                        //处理Collection中的泛型
                        List<FieldDocStorer> list = new ArrayList<>();
                        ClassDoc classDoc = collectionTypes[0].asClassDoc();
                        for(FieldDoc fieldDoc1 : classDoc.fields(false)){
                            //如果entity中的一个字段又是collection怎么办？递归处理
                            list.add(handleFieldDocImprove(fieldDoc1,new_typeSet));
                        }
                        fieldDocStorer.setTypeArgs(list);
                    }
                }
            }else if(SnugUtil.isMap(type)){
                fieldDocStorer.setMap(true);
                Type[] mapTypes = type.asParameterizedType().typeArguments();
                if(mapTypes!=null && mapTypes.length>0){
                    for(Type t : mapTypes){
                        if(SnugUtil.isEntity(type)){
                            //处理Map中的泛型
                            List<FieldDocStorer> list = new ArrayList<>();
                            ClassDoc classDoc = t.asClassDoc();
                            for(FieldDoc fieldDoc1 : classDoc.fields(false)){
                                list.add(handleFieldDocImprove(fieldDoc1,new_typeSet));
                            }
                            fieldDocStorer.setTypeArgs(list);
                        }
                    }
                }
            }else{
                fieldDocStorer.setBasic(true);
            }
        }
        return fieldDocStorer;
    }

    /**
     *  处理fieldDoc进阶版，对循环依赖问题进行处理。
     *  利用typeSet处理。类似于方法调用的原理。
     * @param fieldDoc
     * @param typeSet
     * @return
     */
    public FieldDocStorer handleFieldDocImprove(FieldDoc fieldDoc,Set<String> typeSet){
        return handleFieldDocImprove(fieldDoc,typeSet,null);
    }
//    public FieldDocStorer handleFieldDocImprove(FieldDoc fieldDoc,Set<String> typeSet){
//        if(fieldDoc==null){
//            return null;
//        }
//        FieldDocStorer fieldDocStorer = new FieldDocStorer();
//        fieldDocStorer.setName(fieldDoc.name());
//        fieldDocStorer.setType(fieldDoc.type().toString());
//        fieldDocStorer.setComment(fieldDoc.commentText());
//
//        if(typeSet.contains(fieldDoc.type().toString())){
//            return fieldDocStorer;
//        }else{
//            typeSet.add(fieldDoc.type().toString());
//        }
//        Set<String> new_typeSet = new HashSet<String>();
//        for(String str : typeSet){
//            new_typeSet.add(str);
//        }
//
//        if(SnugUtil.isEntity(fieldDoc.type())){
//            fieldDocStorer.setEntity(true);
//            if(fieldDocStorer.isArray()){
//                fieldDocStorer.setArray(true);
//            }
//            List<FieldDocStorer> list = new ArrayList<>();
//            ClassDoc classDoc = fieldDoc.type().asClassDoc();
//            for(FieldDoc fieldDoc1 : classDoc.fields(false)){
//                list.add(handleFieldDocImprove(fieldDoc1,new_typeSet));
//            }
//            fieldDocStorer.setTypeArgs(list);
//
//            //将fieldDocStorer添加到model中,去掉数组类型。
//            String fieldDocStorerType = fieldDocStorer.getType();
//            if(fieldDocStorerType.contains("[]")){
//                fieldDocStorerType = fieldDocStorerType.replace("[","").replace("]","");
//            }
//            if(!snugDocContext.models.containsKey(fieldDocStorerType)){
//                snugDocContext.models.put(fieldDocStorerType,fieldDocStorer);
//            }
//
//        }else{
//            if(SnugUtil.isArray(fieldDoc.type())){
//                fieldDocStorer.setArray(true);
//            }else if(SnugUtil.isCollection(fieldDoc.type())){
//                fieldDocStorer.setCollection(true);
//                Type[] collectionTypes = fieldDoc.type().asParameterizedType().typeArguments();
//                if(collectionTypes!=null && collectionTypes.length>0){
//                    if(SnugUtil.isEntity(collectionTypes[0])){
//                        //处理Collection中的泛型
//                        List<FieldDocStorer> list = new ArrayList<>();
//                        ClassDoc classDoc = collectionTypes[0].asClassDoc();
//                        for(FieldDoc fieldDoc1 : classDoc.fields(false)){
//                            //如果entity中的一个字段又是collection怎么办？递归处理
//                            list.add(handleFieldDocImprove(fieldDoc1,new_typeSet));
//                        }
//                        fieldDocStorer.setTypeArgs(list);
//                    }
//                }
//            }else if(SnugUtil.isMap(fieldDoc.type())){
//                fieldDocStorer.setMap(true);
//                Type[] mapTypes = fieldDoc.type().asParameterizedType().typeArguments();
//                if(mapTypes!=null && mapTypes.length>0){
//                    for(Type type : mapTypes){
//                        if(SnugUtil.isEntity(type)){
//                            //处理Map中的泛型
//                            List<FieldDocStorer> list = new ArrayList<>();
//                            ClassDoc classDoc = type.asClassDoc();
//                            for(FieldDoc fieldDoc1 : classDoc.fields(false)){
//                                list.add(handleFieldDocImprove(fieldDoc1,new_typeSet));
//                            }
//                            fieldDocStorer.setTypeArgs(list);
//                        }
//                    }
//                }
//            }else{
//                fieldDocStorer.setBasic(true);
//            }
//        }
//        return fieldDocStorer;
//    }

    /**
     * 对fieldDoc进行处理，未处理循环依赖的版本。
     * @param fieldDoc
     * @return
     */
    public FieldDocStorer handleFieldDoc(FieldDoc fieldDoc){
        if(fieldDoc==null){
            return null;
        }
        FieldDocStorer fieldDocStorer = new FieldDocStorer();
        fieldDocStorer.setName(fieldDoc.name());
        fieldDocStorer.setType(fieldDoc.type().toString());
        fieldDocStorer.setComment(fieldDoc.commentText());
        if(SnugUtil.isEntity(fieldDoc.type())){
            fieldDocStorer.setEntity(true);
            if(fieldDocStorer.isArray()){
                fieldDocStorer.setArray(true);
            }
            List<FieldDocStorer> list = new ArrayList<>();
            ClassDoc classDoc = fieldDoc.type().asClassDoc();
            for(FieldDoc fieldDoc1 : classDoc.fields(false)){
                list.add(handleFieldDoc(fieldDoc1));
            }
            fieldDocStorer.setTypeArgs(list);

        }else{
            if(SnugUtil.isArray(fieldDoc.type())){
                fieldDocStorer.setArray(true);
            }else if(SnugUtil.isCollection(fieldDoc.type())){
                fieldDocStorer.setCollection(true);
                Type[] collectionTypes = fieldDoc.type().asParameterizedType().typeArguments();
                if(collectionTypes!=null){
                    if(SnugUtil.isEntity(collectionTypes[0])){
                        //处理Collection中的泛型
                        List<FieldDocStorer> list = new ArrayList<>();
                        ClassDoc classDoc = collectionTypes[0].asClassDoc();
                        for(FieldDoc fieldDoc1 : classDoc.fields(false)){
                            //如果entity中的一个字段又是collection怎么办？
                            list.add(handleFieldDoc(fieldDoc1));
                        }
                        fieldDocStorer.setTypeArgs(list);
                    }else{
                        //如果泛型不是实体怎么处理

                    }
                }
            }else if(SnugUtil.isMap(fieldDoc.type())){
                fieldDocStorer.setMap(true);
                Type[] mapTypes = fieldDoc.type().asParameterizedType().typeArguments();
                if(mapTypes!=null){
                    for(Type type : mapTypes){
                        if(SnugUtil.isEntity(type)){
                            //处理Map中的泛型
                            List<FieldDocStorer> list = new ArrayList<>();
                            ClassDoc classDoc = type.asClassDoc();
                            for(FieldDoc fieldDoc1 : classDoc.fields(false)){
                                list.add(handleFieldDoc(fieldDoc1));
                            }
                            fieldDocStorer.setTypeArgs(list);
                        }
                    }
                }else{
                    //如果泛型不是实体怎么处理

                }
            }else{
                fieldDocStorer.setBasic(true);
            }
        }
        return fieldDocStorer;
    }

}
