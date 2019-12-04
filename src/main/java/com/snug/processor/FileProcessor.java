package com.snug.processor;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FileProcessor {

    public static void main(String[] args) {
//        String path = System.getProperty("user.dir");
//        System.out.println(path);
//        getJavaFiles(path);
//
//        ArrayList<String> list = getRootDir("/Users/abner/abner/idea_workspace/demo");
//        System.out.println(list.toString());

//        String path = "/Users/abner/abner_repo/ifp-project-demo-rest";
//        List<File> targetlist = getDirsByName(path,"target");
//        List<String> libs = getJarsfromLib(targetlist);
//        for(String jarName : libs){
//            System.out.println(jarName);
//        }

        List<File> files = getDirsByName("~/abner/test","lib");
        System.out.println(files);

    }

    public static ArrayList<String> getModuleDir(Object obj){
        File directory = null;
        if (obj instanceof File) {
            directory = (File) obj;
        } else {
            directory = new File(obj.toString());
        }
        ArrayList<String> paths = new ArrayList<String>();
        if(!directory.isDirectory()){
            return null;
        }
        paths.add(directory.getAbsolutePath());
        File[] fileArr = directory.listFiles();
        for (int i = 0; i < fileArr.length; i++) {
            File fileOne = fileArr[i];
            if(fileOne.isDirectory()){
                String fileOneName = fileOne.getName();
                if(!"src".equals(fileOneName) && !"target".equals(fileOneName)
                        && !fileOneName.startsWith(".")){
                    paths.add(fileOne.getAbsolutePath());
                }

            }
        }
        return paths;
    }

    public static List<File> getJavaFiles(Object obj){
        return getFilesBySuffix(obj,"java");
    }

    public static ArrayList<String> getClassFilePaths(Object obj){
        List<File> files = getFilesBySuffix(obj,"class");
        ArrayList<String> paths = new ArrayList<String>();
        for(File file : files){
            paths.add(file.getAbsolutePath());
        }
        return paths;
    }

    public static List<String> getDependencyJars(String path){
        List<String> jars = null;
        if(CommendProcessor.mvnPackage(path)){
            if(CommendProcessor.releaseJAR(path)){
                List<File> targetList = getDirsByName(path,"target");
                jars = getJarsfromLib(targetList);
            }else{
                log.error("解析jar包失败");
            }
        }
        return jars;
    }

    /**
     * 获取打包后解压jar包后在lib文件夹中所依赖的jar包。
     * @param targetList
     * @return
     */
    public static List<String>  getJarsfromLib(List<File> targetList){
        List<String> jars = new ArrayList<>();
        if(targetList!=null){
            for(File target : targetList){
                List<File> libs = getDirsByName(target,"lib");
                if(libs!=null && libs.size()>0){
                    for(File lib : libs){
                        for(File jar : lib.listFiles()){
                            jars.add(jar.getAbsolutePath());
                        }
                    }
                }
            }
        }else{
            log.error("targetList为null");
        }
        return jars;
    }

    /**
     * 获取打包后生成的jar
     * @param targetList
     * @return
     */
    public static List<String> getJarFromTarget(List<File> targetList){
        List<String> jarList = new ArrayList<>();
        for(File target : targetList){
            if(target.isDirectory()){
                for(File file : target.listFiles()){
                    if(file.isFile() && file.getName().endsWith("jar")){
                        jarList.add(file.getAbsolutePath());
                    }
                }
            }
        }
        return jarList;
    }

    public static List<File> getFilesBySuffix(Object obj,String suffixName) {
        File directory = null;
        if (obj instanceof File) {
            directory = (File) obj;
        } else {
            directory = new File(obj.toString());
        }
        ArrayList<File> files = new ArrayList<File>();
        if (directory.isFile()) {
            String fileName = directory.getName();
            String suffix = fileName.substring(fileName.lastIndexOf(".")+1);
            if(suffixName!=null && suffixName.equals(suffix)){
                files.add(directory);
            }
            return files;
        } else if (directory.isDirectory()) {
            File[] fileArr = directory.listFiles();
            for (int i = 0; i < fileArr.length; i++) {
                File fileOne = fileArr[i];
                files.addAll(getFilesBySuffix(fileOne,suffixName));
            }
        }
        return files;
    }

    /**
     * 找到指定的目录
     * @param obj
     * @param dirName
     * @return
     */
    public static List<File> getDirsByName(Object obj,String dirName) {
        File directory = null;
        if (obj instanceof File) {
            directory = (File) obj;
        } else {
            directory = new File(obj.toString());
        }
        ArrayList<File> files = new ArrayList<File>();
        if (directory.isDirectory() && directory.getName().equals(dirName)) {
            files.add(directory);
            return files;
        } else if (directory.isDirectory()) {
            File[] fileArr = directory.listFiles();
            for (int i = 0; i < fileArr.length; i++) {
                File fileOne = fileArr[i];
                files.addAll(getDirsByName(fileOne,dirName));
            }
        }
        return files;
    }

}
