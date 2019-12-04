package com.snug.storer;

import com.sun.javadoc.ClassDoc;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Data
@ToString
public class DubboStorer {
    ClassDoc classdoc;
    List<ClassDoc> implementInterDocs;
    /**
     * 类名
     */
    String name;
    Map<String,String> dubboAnnotationTags;
    String comment;
    Map<String,String> dubboClassTags;
    List<MethodStorer> methods;
}
