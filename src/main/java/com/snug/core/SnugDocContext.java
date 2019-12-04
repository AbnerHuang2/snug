package com.snug.core;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.snug.propertities.SnugPropertities;
import com.snug.storer.DubboStorer;
import com.snug.storer.FieldDocStorer;
import com.snug.storer.MethodStorer;
import com.snug.processor.FileProcessor;
import com.snug.util.SnugUtil;
import com.sun.javadoc.Doclet;
import com.sun.prism.shader.Texture_ImagePattern_AlphaTest_Loader;
import com.sun.tools.javadoc.Main;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

@Slf4j
@Data
public class SnugDocContext {

    List<DubboStorer> dubboClasses;

    /**
     *  String ： model类型
     *  FieldDocStorer ：model类
     */
    Map<String, FieldDocStorer> models; //显示model的时候记得把数组类型的[]去掉。

    SnugPropertities snugPropertities;

    public SnugDocContext(SnugPropertities snugPropertities){
        this.snugPropertities = snugPropertities;
        //初始化
        this.dubboClasses = new ArrayList<DubboStorer>();
        this.models = new HashMap<String,FieldDocStorer>();
    }

    /**
     * 解析相应的Java文件
     * @param doclet
     * @param rootPath
     */
    public void execute(SnugDoclet doclet,String rootPath){
        //执行javadoc 命令
        String[] args = getExecuteArgs(doclet,rootPath);
        if(args!=null){
            Main.execute(args);
        }

    }

    private String[] getExecuteArgs(Doclet doclet, String rootPath){
        List<File> javaFiles = FileProcessor.getJavaFiles(rootPath);
        ArrayList<String> args = new ArrayList<String>();
        args.add("-doclet");
        args.add(doclet.getClass().getName());
        args.add("-encoding");
        args.add("UTF-8");  //编码
        args.add("-quiet");
        args.add("-classpath");

        //获取外部jar包
        List<String> jars = FileProcessor.getDependencyJars(rootPath);
        if(jars==null || jars.size()<1){
            log.error("无法获取外部jar包，该项目可能不符合dubbo项目部署条件");
            System.exit(-1);
        }
        args.add(SnugUtil.join(jars,":"));

        if(javaFiles!=null && javaFiles.size()>0){
            String[] javaPaths = new String[javaFiles.size()];
            for(int i=0;i<javaFiles.size();i++){
                javaPaths[i] = javaFiles.get(i).getAbsolutePath();
            }
            args.addAll(Arrays.asList(javaPaths));

            //添加路径
            args.add("-sourcepath");
            args.add(rootPath);

            return args.toArray(new String[args.size()]);
        }else{
            System.err.println("没有Java文件");
            return null;
        }
    }

}
