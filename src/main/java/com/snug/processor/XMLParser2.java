package com.snug.processor;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.*;

/**
 * 这个类本来是打算用来解析pom文件，通过更改相关配置生成外部依赖jar包的lib文件夹。
 * 但是通过maven打包和解压的方式能够获取到，就不用解析了。所以这个类暂时没有什么用。
 * @program: demo
 * @author: Abner
 * @create: 2019-12-02 19:53
 **/
public class XMLParser2 {
    public static void main(String[] args) {
        String path = "/Users/abner/abner/idea_workspace/smalldoc_test/pom.xml";
        parse(path);
    }

    public static void parse(String path){
        SAXReader reader = new SAXReader();
        reader.setEncoding("utf-8");
        Document document = null;
        try {
            document = reader.read(new File(path));

            Element root = document.getRootElement();
            Element build = root.element("build");
            if(build==null){
                build = root.addElement("build");
            }
            Element plugins = build.element("plugins");
            if(plugins==null){
                plugins = build.addElement("plugins");
            }
            boolean hasDependencyPlugin = false;
            for(Element plugin : plugins.elements()){
                if("maven-dependency-plugin".equals(plugin.element("artifactId").getTextTrim())){
                    hasDependencyPlugin = true;
                    break;
                }
            }
            //如果pom中没有dependency-plugin,添加一个
            if(!hasDependencyPlugin){
                addPlugin(plugins);
            }

            //记得保存
            saveXML(document,path);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void addPlugin(Element plugins){
        Element plugin = plugins.addElement("plugin");
        Element groupId = plugin.addElement("groupId");
        groupId.setText("org.apache.maven.plugins");

        Element artifactId = plugin.addElement("artifactId");
        artifactId.setText("maven-dependency-plugin");

        Element version = plugin.addElement("version");
        version.setText("2.10");

        Element executions = plugin.addElement("executions");
        Element execution = executions.addElement("execution");
        Element id = execution.addElement("id");
        id.setText("copy");
        Element phase = execution.addElement("phase");
        phase.setText("package");

        Element goals = execution.addElement("goals");
        Element goal = goals.addElement("goal");
        goal.setText("copy-dependencies");

        Element configuration = execution.addElement("configuration");
        Element outputDirectory = configuration.addElement("outputDirectory");
        outputDirectory.setText("${project.build.directory}/lib");

    }

    public static void saveXML(Document document,String path) {
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("utf-8");
        try{
            XMLWriter writer = new XMLWriter(new FileOutputStream(path),format);

            writer.write(document);
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
