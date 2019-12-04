package com.snug.storer;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class MethodStorer {
    ClassDoc classDoc;
    MethodDoc methodDoc;
    String name;
    String comment;
    Map<String,String> tags;
    Map<String, FieldDocStorer> paraMap;
    FieldDocStorer returnType;
}
