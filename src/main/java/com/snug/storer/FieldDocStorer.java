package com.snug.storer;

import com.sun.javadoc.FieldDoc;
import lombok.Data;

import java.util.List;

@Data
public class FieldDocStorer {
    String name;
    String type;
    String comment;
    boolean isEntity;
    boolean isGeneric;
    boolean isBasic;
    boolean isArray;
    boolean isCollection;
    boolean isMap;
    /**
     * 当这个字段是一个entity时，typeArgs是它的所有的字段。
     */
    List<FieldDocStorer> typeArgs;

}
