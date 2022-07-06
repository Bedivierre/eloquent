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
        QueryBuilder query = conn.query();
        query.where("credentials", DBWhereOp.LIKE, "qw%")
            .orWhere((q) -> {
                q.where("credentials", DBWhereOp.LIKE, "Na%")
                    .where("time", DBWhereOp.GE, 4);
            });

        String s = query.toSql(TestModel.class);

        System.out.println(s);
        Map<String, Object> update = new HashMap<>();
        update.put("type", 2);
        ResultSet<TestModel> result =  query.get(TestModel.class);
        query.update(update, TestModel.class);
        result =  query.get(TestModel.class);
        TestModel r =  conn.find(102, TestModel.class);
        System.exit(0);
    }
}