package com.snug;

import com.alibaba.fastjson.JSONObject;
import com.snug.core.DefaultSnugDocletImpl;
import com.snug.core.SnugDocContext;
import com.snug.core.SnugDoclet;
import com.snug.propertities.SnugPropertities;
import com.snug.transformer.JsonTransformer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class Application {

    public static void main(String[] args) {
        //System.setProperty("logback.configurationFile",System.getProperty("user.dir")+"/src/main/resources.logback.xml");
        String rootPath =
                //"/Users/abner/abner/idea_workspace/smalldoc_test";
                "/Users/abner/abner_repo/ifp-project-demo-rest";
        if(args.length==1){
            rootPath = args[0];
        }
        Application.parse(rootPath);
    }

    public static void parse(String rootPath){
        SnugPropertities snugPropertities = new SnugPropertities();
        snugPropertities.setProjectPath(rootPath);
        SnugDocContext context = new SnugDocContext(snugPropertities);
        //默认是解析dubbo接口
        SnugDoclet doclet = new DefaultSnugDocletImpl(context);

        context.execute(doclet,rootPath);

        log.info("获取到JSON");
        JSONObject docs = JsonTransformer.getInstance().exportJSON(context.getDubboClasses(),context.getSnugPropertities());
        System.out.println(docs);
        //将信息导出到文件中
        File file = new File("temp.log");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(docs.toJSONString().getBytes());

            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //获取项目中的class路径，包括所依赖的jar包的路径
        //System.out.println(System.getProperty("java.class.path"));
    }

}
