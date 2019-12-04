package com.snug.processor;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;

/**
 * 执行一些cmd命令，主要是maven打包和jar解压。主要是mac下的命令，目前不支持其他操作系统。
 * 不能直接通过打包和解压获取项目依赖jar包，因为dubbo一般是多模块的。
 * @program: demo
 * @author: Abner
 * @create: 2019-11-30 11:01
 **/
@Slf4j
public class CommendProcessor {
    public static void main(String[] args) {
        String path = "/Users/abner/abner_repo/ifp-project-demo-rest";
        mvnPackage(path);
        releaseJAR(path);
    }

    public static boolean mvnPackage(String path){
        boolean b = execute("/usr/local/bin/mvn clean package -f "+path+"/pom.xml",path,true);
        if(!b){
            log.error("该项目还不具备部署条件");
        }
        return b;
    }

    /**
     * 说明：
     *      解压的信息在本项目的根目录下。
     * @param path  传入的是target目录。
     * @return
     */
    public static boolean releaseJAR(String path){
        boolean b = false;
        String javahome = System.getProperty("java.home");
        File file = new File(path);
        if(!file.isDirectory()){
            return false;
        }
        List<File> targetList = FileProcessor.getDirsByName(path,"target");
        int flag = 0;
        if(targetList.size()>0){
            List<String> jarList = FileProcessor.getJarFromTarget(targetList);
            for(String jarName : jarList){
                log.info("解压 "+jarName);
                if(!execute(javahome+"/../bin/jar xvf "+jarName,jarName.substring(0,jarName.lastIndexOf("/")),false)){
                    log.error("解压"+jarName+"失败");
                    flag++;
                }
            }
            log.info("解压jar包完成");
            if(flag==0){
                b = true;
            }
        }
        if(!b){
            log.error("解压jar包失败,失败个数 : "+flag);
        }
        return b;
    }

    public static boolean execute(String cmd,String dir,boolean printOrNot) {
        log.info("执行命令 ： "+cmd);
        boolean b = false;
        Runtime runtime=Runtime.getRuntime();
        try {
            Process process = runtime.exec(cmd,null,new File(dir));
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            if(printOrNot){
                while ((line = input.readLine()) != null) {
                    System.out.println(line);   //输出控制台信息
                }
            }else{
                while ((line = input.readLine()) != null) {

                }
            }
            input.close();

            //process/waitFor()可能会导致进程死锁。
            //所以需要主进程在waitfor之前，能不断处理缓冲区中的数据就可以。
            int res = process.waitFor();
            if(res==0){
                b = true;
            }

        } catch (Exception e) {
            log.error("执行命令 "+cmd+" 失败, message : "+e.getMessage());

        }
        return b;
    }

}
