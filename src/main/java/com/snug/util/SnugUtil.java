package com.snug.util;

import com.sun.javadoc.Type;
import java.util.ArrayList;
import java.util.List;

public class SnugUtil {

    /**
     * 包括基本类型的数组和实体数组
     * @param type
     * @return
     */
    public static boolean isArray(Type type){
        return !type.dimension().equals("");
    }

    public static boolean isCollection(Type type){
        return type.qualifiedTypeName().endsWith("List") || type.qualifiedTypeName().endsWith("Set");
    }

    public static boolean isMap(Type type){
        return type.qualifiedTypeName().endsWith("Map");
    }

    /**
     * 判断类型是否为库类型。默认基本类型，java包，javax包，spring包为库类型。
     *
     * @param ptype
     * @return
     */
    public static boolean isBasic(Type ptype) {
        String qualifiedTypeName;
        return ptype.isPrimitive() ||
                (qualifiedTypeName = ptype.qualifiedTypeName()).startsWith("java.") ||
                qualifiedTypeName.startsWith("javax.") ||
                qualifiedTypeName.startsWith("org.springframework.");
    }

    public static boolean isEntity(Type type){
        return !isBasic(type);
    }

    /**
     * 字符串拼接
     * @param list
     * @param split
     * @return
     */
    public static String join(List<String> list,String split){
        if(list==null || list.size()<1){
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<list.size();i++){
            if(i==list.size()-1){
                sb.append(list.get(i));
            }else{
                sb.append(list.get(i)+split);
            }
        }
        return sb.toString();
    }

    /**
     *  如果一个对象为null，把它替换为""
     * @param obj
     * @return
     */
    public String replaceNull(Object obj){
        return obj == null ? "" : obj.toString();
    }

    public static void main(String[] args) {
        System.out.println(System.getProperty("java.class.path"));
    }
}
