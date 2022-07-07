package com.bedivierre.eloquent.dialects;

import com.bedivierre.eloquent.QueryBuilder;
import com.bedivierre.eloquent.expr.DBQueryWhere;
import com.bedivierre.eloquent.model.DBModel;
import com.bedivierre.eloquent.utils.Util;

import java.sql.SQLException;
import java.util.Map;

/*********************************
 ** Code by Bedivierre
 ** 07.07.2022 10:44
 **********************************/
public abstract class SQLDialect {

    public abstract <T extends DBModel> String selectStatement(QueryBuilder<T> query)
            throws InstantiationException, IllegalAccessException, SQLException;
    public abstract <T extends DBModel> String findStatement(QueryBuilder<T> query)
            throws InstantiationException, IllegalAccessException, SQLException ;
    public abstract <T extends DBModel> String aggregateStatement(QueryBuilder<T> query, String aggregate)
            throws InstantiationException, IllegalAccessException, SQLException ;

    public abstract <T extends DBModel> String insertStatement(QueryBuilder<T> query)
            throws InstantiationException, IllegalAccessException, SQLException ;
    public abstract <T extends DBModel> String updateStatement(QueryBuilder<T> query)
            throws InstantiationException, IllegalAccessException, SQLException ;
    public abstract <T extends DBModel> String deleteStatement(QueryBuilder<T> query)
            throws InstantiationException, IllegalAccessException, SQLException ;

    public abstract <T extends DBModel> String dropStatement(QueryBuilder<T> query)
            throws InstantiationException, IllegalAccessException, SQLException ;
    public abstract <T extends DBModel> String truncateStatement(QueryBuilder<T> query)
            throws InstantiationException, IllegalAccessException, SQLException ;

    public <T extends DBModel> String buildQuery(QueryBuilder<T> query)
            throws SQLException, InstantiationException, IllegalAccessException {
//        if(!query.isChanged())
//            return queryText;
        String queryText = "";
        if(query.getQueryType().equals("find"))
            queryText = findStatement(query);
        if(query.getQueryType().equals("select"))
            queryText = selectStatement(query);
        if(query.getQueryType().equals("insert"))
            queryText = insertStatement(query);
        if(query.getQueryType().equals("update"))
            queryText = updateStatement(query);
        if(query.getQueryType().equals("delete"))
            queryText = deleteStatement(query);
        if(query.getQueryType().equals("count") || query.getQueryType().equals("min") || query.getQueryType().equals("max")
                || query.getQueryType().equals("avg") || query.getQueryType().equals("sum"))
            queryText = aggregateStatement(query, query.getQueryType());
        //query.setChanged(false);
        return queryText;
    }



    public abstract <T extends DBModel> String whereExpr(DBQueryWhere<T> expr);

}
