package com.snug.core;

import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.RootDoc;

public abstract class SnugDoclet extends Doclet {

    protected static RootDoc rootDoc;
    /**
     *  doclet
     */
    protected static SnugDoclet doclet;

    protected SnugDocContext snugDocContext;

    public SnugDoclet(SnugDocContext snugDocContext){
        this.snugDocContext = snugDocContext;
    }

    /**
     * 想要自己处理RootDoc必须要重写这个方法
     * @param root
     * @return
     */
    public static boolean start(RootDoc root) {
        rootDoc = root;
        return doclet.process(root);
    }

    public void setDoclet(SnugDoclet doclet){
        SnugDoclet.doclet = doclet;
    }

    /**
     * 让子类获取到RootDoc后自己做处理
     * @param root
     * @return
     */
    protected abstract boolean process(RootDoc root);

    /**
     * 返回 {@link LanguageVersion#JAVA_1_5} 避免获取结构信息时泛型擦除（即使Compiler Tree API已经处理了泛型）
     *
     * @return
     */
    public static LanguageVersion languageVersion() {
        return LanguageVersion.JAVA_1_5;
    }

}
