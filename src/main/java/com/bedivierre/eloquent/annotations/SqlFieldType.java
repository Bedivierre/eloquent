package com.bedivierre.eloquent.annotations;

import java.util.HashMap;
import java.util.Map;

/*********************************
 ** Code by Bedivierre
 ** 15.07.2022 12:06
 **********************************/
public enum SqlFieldType {
    NONE("none"),

    STRING("string"),
    FLOAT("float"),
    DECIMAL("decimal"),
    DOUBLE("double"),
    INT("int"),
    LONG("long"),
    BOOLEAN("boolean"),

    ARRAY("array"),
    MAP("map"),

    DATE("date"),
    TIME("time"),
    DATETIME("datetime"),
    TIMESTAMP("timestamp"),

    ;

    public final String value;

    SqlFieldType(String value) {
        this.value = value;
    }


    private static final Map<String, SqlFieldType> BY_VALUE = new HashMap<>();

    static {
        for (SqlFieldType e : values()) {
            BY_VALUE.put(e.value, e);
        }
    }

    public static SqlFieldType getByValue(String number) {
        return BY_VALUE.get(number);
    }
}
