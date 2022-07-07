package com.bedivierre;

import com.bedivierre.eloquent.DB;
import com.bedivierre.eloquent.QueryBuilder;
import com.bedivierre.eloquent.ResultSet;
import com.bedivierre.eloquent.TestModel;
import com.bedivierre.eloquent.expr.DBWhereOp;

import java.util.HashMap;
import java.util.Map;

/*********************************
 ** Code by Bedivierre
 ** $DATE $TIME
 **********************************/
public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello world!");
        DB conn = new DB("localhost", "ins", "root", "");
        QueryBuilder<TestModel> query = conn.query(TestModel.class).whereIn("id", 99, 101, 105);
        query.orWhereNot((q) -> {
            q.where("credentials", DBWhereOp.LIKE, "qw%")
                    .whereNot("time", DBWhereOp.GR, 3);
        }).select("id", "credentials");
        String s = query.toSql();
        ResultSet<TestModel> result = query.get();


        int v = conn.where(TestModel.class, "credentials", "q").count();


        System.out.println(s);

        TestModel r =  conn.find(102, TestModel.class);
        System.exit(0);
    }
}