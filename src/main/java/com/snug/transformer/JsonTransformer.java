package com.snug.transformer;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.snug.propertities.SnugPropertities;
import com.snug.storer.DubboStorer;
import com.snug.storer.FieldDocStorer;
import com.snug.storer.MethodStorer;

import java.util.List;
import java.util.Map;

/**
 * @program: demo
 * @author: Abner
 * @create: 2019-12-03 13:43
 **/
public class JsonTransformer {

    /**
     * 文档结构，包含工程以外的信息
     */
    private  JSONObject docsJSON;

    private static volatile JsonTransformer jsonTransformer;

    public static JsonTransformer getInstance(){
        if(jsonTransformer == null){
            synchronized (JsonTransformer.class){
                if(jsonTransformer == null){
                    jsonTransformer = new JsonTransformer();
                }
            }
        }
        return jsonTransformer;
    }

    private JsonTransformer(){

    }

    public JSONObject exportJSON(List<DubboStorer> dubboClasses, SnugPropertities snugPropertities){
        this.docsJSON = new JSONObject();

        docsJSON.put("ImportData",getMessthodsJSONArray(dubboClasses));
        docsJSON.put("projectId",snugPropertities.getProjectName());

        return this.docsJSON;
    }

    public JSONArray getMessthodsJSONArray(List<DubboStorer> dubboStorers){
        JSONArray jsonArray = new JSONArray();
        if(dubboStorers==null || dubboStorers.size()<1){
            return  jsonArray;
        }
        for(DubboStorer storer : dubboStorers){
            for(MethodStorer methodStorer : storer.getMethods()){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name",methodStorer.getName());
                jsonObject.put("group_name",storer.getDubboAnnotationTags().get("group"));
                jsonObject.put("status",storer.getDubboAnnotationTags().get("status"));
                jsonObject.put("tags",storer.getDubboClassTags().keySet());

                JSONObject dubboMethod = new JSONObject();
                if(storer.getImplementInterDocs()!=null || storer.getImplementInterDocs().size()>1){
                    dubboMethod.put("service",storer.getImplementInterDocs().get(0).qualifiedName());
                }else{
                    dubboMethod.put("service","未实现接口");
                }
                dubboMethod.put("method",methodStorer.getName());
                dubboMethod.put("method_annotation",methodStorer.getComment());
                dubboMethod.put("timeout",storer.getDubboAnnotationTags().get("timeout"));
                dubboMethod.put("retry",storer.getDubboAnnotationTags().get("retries"));
                dubboMethod.put("group",storer.getDubboAnnotationTags().get("group"));
                dubboMethod.put("provider_version",storer.getDubboAnnotationTags().get("version"));

                JSONArray argsArray = new JSONArray();
                for( Map.Entry<String, FieldDocStorer> entry : methodStorer.getParaMap().entrySet()){
                    JSONObject json = getParaJSON(entry.getValue());

                    argsArray.add(json);
                }
                dubboMethod.put("args",argsArray);

                jsonObject.put("dubbo",dubboMethod);
                jsonArray.add(jsonObject);
            }


        }
        return jsonArray;
    }

    public JSONObject getParaJSON(FieldDocStorer para){
        JSONObject obj = new JSONObject();
        if(para!=null){
            obj.put("name",para.getName());
            obj.put("type",para.getType());
            if(para.getTypeArgs()!=null && para.getTypeArgs().size()>0){
                for(FieldDocStorer fieldDocStorer : para.getTypeArgs()){
                    if(para.isEntity() && para.isArray()){
                        String type = para.getType().replace("[","").replace("]","");
                        obj.put(type,getParaJSON(fieldDocStorer));
                    }else{
                        obj.put(para.getType(),getParaJSON(fieldDocStorer));
                    }
                }
            }
        }
        return obj;
    }

}
