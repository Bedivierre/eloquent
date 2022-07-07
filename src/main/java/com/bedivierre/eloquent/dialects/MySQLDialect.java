package com.bedivierre.eloquent.dialects;

import com.bedivierre.eloquent.QueryBuilder;
import com.bedivierre.eloquent.expr.DBQueryWhere;
import com.bedivierre.eloquent.expr.DBWhereOp;
import com.bedivierre.eloquent.model.DBModel;
import com.bedivierre.eloquent.utils.Util;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/*********************************
 ** Code by Bedivierre
 ** 07.07.2022 10:44
 **********************************/
public class MySQLDialect extends SQLDialect{


    private <T extends DBModel> String processColumns(QueryBuilder<T> query){
        if(query.getColumns().size() == 0)
            return "*";
        StringBuilder b = new StringBuilder();
        for(int i = 0; i < query.getColumns().size(); i++){
            String col = query.getColumns().get(i);
            if(col.equals("*"))
                return "*";
            b.append(Util.processIdentifier(col));
            if(i < query.getColumns().size() - 1){
                b.append(",");
            }
        }

        return b.toString();
    }
    private <T extends DBModel> String processOrderBy(QueryBuilder<T> query){
        if(query.getOrderByColumns() == null || query.getOrderByColumns().size() == 0)
            return "";
        StringBuilder b = new StringBuilder();
        for(int i = 0; i < query.getOrderByColumns().size(); i++){
            String col = query.getOrderByColumns().get(i);
            b.append(Util.processIdentifier(col));
            if(i < query.getOrderByColumns().size() - 1){
                b.append(",");
            }
        }
        b.append(query.isOrderByAsc() ? " ASC" : " DESC");
        return b.toString();
    }


    @Override
    public <T extends DBModel> String selectStatement(QueryBuilder<T> query)
            throws InstantiationException, IllegalAccessException, SQLException {
        T instance = query.getModelInstance();

        StringBuilder b = new StringBuilder();
        b.append("SELECT ");
        if(query.isDistinct())
            b.append("DISTINCT ");
        b.append(processColumns(query)).append(" FROM ");
        b.append(Util.processIdentifier(instance.getTable()));

        if(query.getParentWhere().getChildren().size() > 0){
            b.append(" WHERE ").append(query.getParentWhere().toSql());
        }
        if(query.getLimit() > 0){
            b.append(" LIMIT ").append(query.getLimit());
        }
        b.append(processOrderBy(query));

        b.append(";");
        return b.toString();
    }

    @Override
    public <T extends DBModel> String findStatement(QueryBuilder<T> query)
            throws InstantiationException, IllegalAccessException, SQLException {
        T instance = query.getModelInstance();

        return "SELECT " + processColumns(query) + " FROM " +
                Util.processIdentifier(instance.getTable()) +
                " WHERE " + Util.processIdentifier(instance.getPrimaryKey()) +
                "=" + Util.processValue(query.getFindId()) +
                " LIMIT 1;";
    }

    @Override
    public <T extends DBModel> String aggregateStatement(QueryBuilder<T> query, String aggregate)
            throws InstantiationException, IllegalAccessException, SQLException {
        T instance = query.getModelInstance();
        String column = query.getAggregateColumn();
        if(column == null || column.length() == 0)
            column = instance.getPrimaryKey();

        StringBuilder b = new StringBuilder();
        b.append("SELECT ").append(StringUtils.upperCase(aggregate))
                .append("(").append(Util.processIdentifier(column)).append(") AS __number__");
        b.append(" FROM ");
        b.append(Util.processIdentifier(instance.getTable()));

        if(query.getParentWhere().getChildren().size() > 0){
            b.append(" WHERE ").append(query.getParentWhere().toSql());
        }

        b.append(";");
        return b.toString();
    }

    @Override
    public <T extends DBModel> String insertStatement(QueryBuilder<T> query)
            throws InstantiationException, IllegalAccessException, SQLException {
        T instance = query.getModelInstance();;
        if(query.getQuerySetColumns() == null || query.getQuerySetColumns().size() == 0)
            throw new SQLException("Insert parameters is not defined!");

        StringBuilder b = new StringBuilder();
        b.append("INSERT INTO ").append(Util.processIdentifier(instance.getTable()));

        b.append("(");
        List<String> keys = Arrays.asList(query.getQuerySetColumns().keySet().toArray(new String[0]));
        for(int i = 0; i < keys.size(); i++){
            b.append(Util.processIdentifier(keys.get(i)));
            if(i < keys.size() - 1)
                b.append(", ");
        }
        b.append(")");
        b.append(" VALUES ");
        b.append("(");
        List<Object> values = Arrays.asList(query.getQuerySetColumns().values().toArray());
        for(int i = 0; i < values.size(); i++){
            b.append(Util.processValue(values.get(i)));
            if(i < values.size() - 1)
                b.append(", ");
        }
        b.append(")");

        b.append(";");
        return b.toString();
    }

    @Override
    public <T extends DBModel> String updateStatement(QueryBuilder<T> query)
            throws InstantiationException, IllegalAccessException, SQLException {
        T instance = query.getModelInstance();

        if(query.getQuerySetColumns() == null || query.getQuerySetColumns().size() == 0)
            throw new SQLException("Update parameters is not defined!");

        StringBuilder b = new StringBuilder();
        b.append("UPDATE ").append(Util.processIdentifier(instance.getTable()));

        b.append(" SET ");
        query.getQuerySetColumns().forEach((col, val) -> {
            b.append(Util.processIdentifier(col)).append('=').append(Util.processValue(val));
        });

        if(query.getParentWhere().getChildren().size() > 0){
            b.append(" WHERE ").append(query.getParentWhere().toSql());
        }

        b.append(";");
        return b.toString();
    }

    @Override
    public <T extends DBModel> String deleteStatement(QueryBuilder<T> query)
            throws InstantiationException, IllegalAccessException, SQLException {
        T instance = query.getModelInstance();

        StringBuilder b = new StringBuilder();
        b.append("DELETE FROM ").append(Util.processIdentifier(instance.getTable()));

        if(query.getParentWhere().getChildren().size() > 0){
            b.append(" WHERE ").append(query.getParentWhere().toSql());
        }

        b.append(";");
        return b.toString();
    }

    @Override
    public <T extends DBModel> String dropStatement(QueryBuilder<T> query)
            throws InstantiationException, IllegalAccessException, SQLException {
        throw new NotImplementedException();
    }

    @Override
    public <T extends DBModel> String truncateStatement(QueryBuilder<T> query)
            throws InstantiationException, IllegalAccessException, SQLException {
        throw new NotImplementedException();
    }

    @Override
    public <T extends DBModel> String whereExpr(DBQueryWhere<T> expr) {
        StringBuilder b = new StringBuilder();

        if(expr.getChildren().size() > 0) {
            for (int i = 0; i < expr.getChildren().size(); i++) {
                DBQueryWhere<T> wh = expr.getChildren().get(i) instanceof DBQueryWhere
                        ? (DBQueryWhere<T>)expr.getChildren().get(i) : null;
                if(wh == null)
                    continue;

                if(i != 0 )
                    b.append(wh.isAnd() ? " AND " : " OR ");


                if(wh.getChildren().size() > 0) {
                    if(wh.isNot())
                        b.append("NOT ");
                    b.append('(');
                }
                b.append(whereExpr(wh));
                if(wh.getChildren().size() > 0)
                    b.append(')');
            }
        } else if(expr.getOp() == DBWhereOp.IN && expr.getValues() != null && expr.getValues().length > 0) {
            b.append(Util.processIdentifier(expr.getColumn()));
            if(expr.isNot())
                b.append(" NOT");
            b.append(" IN ");
            b.append("(");
            for(int i = 0; i < expr.getValues().length; i++){
                b.append(Util.processValue(expr.getValues()[i]));
                if(i < expr.getValues().length - 1)
                    b.append(",");
            }

            b.append(")");
        } else if(expr.getOp() == DBWhereOp.BETWEEN && expr.getValues() != null && expr.getValues().length >= 2) {
            b.append(Util.processIdentifier(expr.getColumn()));
            if(expr.isNot())
                b.append(" NOT");
            b.append(" BETWEEN ");
            Util.processValue(expr.getValues()[0]);
            b.append(" AND ");
            Util.processValue(expr.getValues()[1]);
        } else {
            if(expr.isNot())
                b.append("NOT ");
            b.append(Util.processIdentifier(expr.getColumn()));
            b.append(' ').append(expr.getOpString()).append(' ');
            b.append(Util.processValue(expr.getValue()));
        }

        return b.toString();
    }
}
