package com.bedivierre.eloquent.expr;

import java.util.HashMap;
import java.util.Map;

/*********************************
 ** Code by Bedivierre
 ** 04.07.2022 11:18
 **********************************/
public enum DBWhereOp {
    EQ("="),
    NE("<>"),
    GR(">"),
    GE(">="),
    LS("<"),
    LE("<="),
    LIKE("LIKE"),
    IN("IN"),
    BETWEEN("BETWEEN");

    public final String value;

    DBWhereOp(String value) {
        this.value = value;
    }


    private static final Map<String, DBWhereOp> BY_VALUE = new HashMap<>();

    static {
        for (DBWhereOp e : values()) {
            BY_VALUE.put(e.value, e);
        }
    }

    public static DBWhereOp getByValue(String number) {
        return BY_VALUE.get(number);
    }
}
