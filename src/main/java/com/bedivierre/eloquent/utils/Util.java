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
        return '`' + value.toString().replace("`", "\\`") + '`';
    }
}
