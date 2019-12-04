package com.snug.processor;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * @program: demo
 * @description: 这是一个XML解析器
 * @author: Abner
 * @create: 2019-11-28 15:05
 **/
public class XMLParser {

    public static void main(String[] args) {
        String path =
                "/Users/abner/abner/idea_workspace/smalldoc_test/pom.xml";
        XMLParser.parse(path);
    }

    public static void parse(String path){
        File file = new File(path);
        try{
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            NodeList nodeList = doc.getElementsByTagName("build");
            if(nodeList!=null){
                if(nodeList.getLength()==1){
                    Node node = nodeList.item(0);
                    NodeList pluginLists = node.getChildNodes();
                    for(int i=0;i<pluginLists.getLength();i++){
                        Node node1 = pluginLists.item(i);
                        if("plugins".equals(node1.getNodeName())){
                            Element element = addPlugin(doc);

                            node1.appendChild(element);

                            //这里只是在内存中完成了添加
                            //还要保存到xml文件中
                            saveXml(doc,path);

                            System.out.println("添加成功");
                        }
                    }
                }else{
                    System.err.println("pom文件有多个build");
                }

            }else{
                //pom文件没有build，抛异常
                System.err.println("pom文件异常");
                //根元素
                //Element element = doc.getDocumentElement();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static Element addPlugin(Document doc){
        Element plugin = doc.createElement("plugin");

        Element groupId = doc.createElement("groupId");
        groupId.setTextContent("org.apache.maven.plugins");

        Element artifactId = doc.createElement("artifactId");
        artifactId.setTextContent("maven-dependency-plugin");

        Element version = doc.createElement("version");
        version.setTextContent("2.10");

        Element executions = doc.createElement("executions");
        Element execution = doc.createElement("execution");
        Element id = doc.createElement("id");
        id.setTextContent("copy");
        Element phase = doc.createElement("phase");
        phase.setTextContent("package");
        Element goals = doc.createElement("goals");

        Element goal = doc.createElement("goal");
        goal.setTextContent("copy-dependencies");
        goals.appendChild(goal);

        Element configuration = doc.createElement("configuration");
        Element outputDirectory = doc.createElement("outputDirectory");
        outputDirectory.setTextContent("${project.build.directory}/lib");
        configuration.appendChild(outputDirectory);

        execution.appendChild(id);
        execution.appendChild(phase);
        execution.appendChild(goals);
        execution.appendChild(configuration);

        executions.appendChild(execution);

        plugin.appendChild(groupId);
        plugin.appendChild(artifactId);
        plugin.appendChild(version);
        plugin.appendChild(executions);

        return plugin;
    }

    //保存xml文件
    public static void saveXml(Document document,String path){

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new FileOutputStream(path));

            transformer.transform(domSource, streamResult);

        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }

    }

}
