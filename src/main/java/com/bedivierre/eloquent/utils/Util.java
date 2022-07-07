package com.bedivierre.eloquent.utils;

/*********************************
 ** Code by Bedivierre
 ** 06.07.2022 12:07
 **********************************/
public final class Util {
    public static String processValue(Object value){
        StringBuilder b = new StringBuilder();
        if(value instanceof Integer || value instanceof Long || value instanceof Float
                || value instanceof Double)
            b.append(value);
        else if(value instanceof Boolean)
            b.append((int)value);
        else
            b.append('\'').append(value.toString().replace("'", "\\'")).append('\'');

        return b.toString();
    }
    public static String processIdentifier(String value){
        StringBuilder b = new StringBuilder();
        String[] ids = value.split("\\.");
        for(int i= 0; i < ids.length; i++){
            b.append('`').append(ids[i].replace("`", "\\`")).append('`');
            if(i < ids.length - 1)
                b.append('.');
        }
        return b.toString();
    }
}
