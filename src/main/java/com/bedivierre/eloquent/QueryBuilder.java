package com.bedivierre.eloquent;

import com.bedivierre.eloquent.expr.DBQueryWhere;
import com.bedivierre.eloquent.expr.DBWhereOp;
import com.bedivierre.eloquent.model.DBModel;
import com.bedivierre.eloquent.utils.Util;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/*********************************
 ** Code by Bedivierre
 ** 01.07.2022 14:15
 **********************************/
public class QueryBuilder {


    private DB connector;
    private List<String> columns;
    private String queryType = "select";
    private boolean changed = true;
    private DBQueryWhere parentWhere;
    private Stack<DBQueryWhere> currentWhereStack;
    private Map<String, Object> querySetColumns;
    String queryText = "";
    private int limit = -1;


    private boolean isChanged(){return changed;}
    public String getQueryType(){return StringUtils.lowerCase(queryType);}
    public DB getConnector() {return connector;}
    public DBQueryWhere getParentWhere() {return parentWhere;}
    public void setConnector(DB conn) {connector = conn;}

    public QueryBuilder(DB connector){
        setConnector(connector);


        columns = new ArrayList<>();
        columns.add("*");
        parentWhere = new DBQueryWhere(this);
        currentWhereStack = new Stack<>();
        currentWhereStack.push(parentWhere);
    }

    public <T extends DBModel>  String toSql(Class<T> model)
            throws InstantiationException, IllegalAccessException, SQLException {
        return buildQuery(model);
    }
    private <T extends DBModel>  String buildQuery(Class<T> model)
            throws InstantiationException, IllegalAccessException, SQLException {
        if(!isChanged())
            return queryText;
        if(getQueryType().equals("select"))
            queryText = buildSelectQuery(model);
        if(getQueryType().equals("update"))
            queryText = buildUpdateQuery(model);
        if(getQueryType().equals("delete"))
            queryText = buildDeleteQuery(model);
        changed = false;
        return queryText;
    }


    //============== query toSql builders
    private <T extends DBModel> String buildFindQuery(Object id, Class<T> model)
            throws InstantiationException, IllegalAccessException, SQLException {

        T instance = model.newInstance();
        String table = instance.getTable();
        if(table.length() == 0){
            throw new SQLException("Table is not defined!");
        }

        StringBuilder b = new StringBuilder();
        b.append("SELECT ").append(processColumns()).append(" FROM ");
        b.append(Util.processIdentifier(table));

        b.append(" WHERE ").append(Util.processIdentifier(instance.getPrimaryKey()))
                .append("=").append(Util.processValue(id));

        b.append(" LIMIT 1;");
        return b.toString();
    }
    private <T extends DBModel>  String buildSelectQuery(Class<T> model)
            throws InstantiationException, IllegalAccessException, SQLException {

        T instance = model.newInstance();
        String table = instance.getTable();
        if(table.length() == 0){
            throw new SQLException("Table is not defined!");
        }

        StringBuilder b = new StringBuilder();
        b.append("SELECT ").append(processColumns()).append(" FROM ");
        b.append(Util.processIdentifier(table));

        if(getParentWhere().getChildren().size() > 0){
            b.append(" WHERE ").append(getParentWhere().toSql());
        }
        if(limit > 0){
            b.append(" LIMIT ").append(limit);
        }

        b.append(";");
        return b.toString();
    }
    private <T extends DBModel>  String buildUpdateQuery(Class<T> model)
            throws InstantiationException, IllegalAccessException, SQLException {
        T instance = model.newInstance();
        String table = instance.getTable();
        if(table.length() == 0){
            throw new SQLException("Table is not defined!");
        }
        if(querySetColumns == null || querySetColumns.size() == 0)
            throw new SQLException("Update parameters is not defined!");

        StringBuilder b = new StringBuilder();
        b.append("UPDATE ").append(Util.processIdentifier(table));

        b.append(" SET ");
        querySetColumns.forEach((col, val) -> {
            b.append(Util.processIdentifier(col)).append('=').append(Util.processValue(val));
        });

        if(getParentWhere().getChildren().size() > 0){
            b.append(" WHERE ").append(getParentWhere().toSql());
        }

        b.append(";");
        return b.toString();
    }
    private <T extends DBModel>  String buildDeleteQuery(Class<T> model)
            throws InstantiationException, IllegalAccessException, SQLException {
        T instance = model.newInstance();
        String table = instance.getTable();
        if(table.length() == 0){
            throw new SQLException("Table is not defined!");
        }

        StringBuilder b = new StringBuilder();
        b.append("DELETE FROM ").append(Util.processIdentifier(table));

        if(getParentWhere().getChildren().size() > 0){
            b.append(" WHERE ").append(getParentWhere().toSql());
        }

        b.append(";");
        return b.toString();
    }


    // columns for select query
    private String processColumns(){
        if(columns.size() == 0)
            return "*";
        StringBuilder b = new StringBuilder();
        for(int i = 0; i < columns.size(); i++){
            String col = columns.get(i);
            if(col.equals("*"))
                return "*";
            b.append(Util.processIdentifier(col));
            if(i < columns.size() - 1){
                b.append(",");
            }
        }

        return b.toString();
    }


    //============== common queries
    public <T extends DBModel> ResultSet<T> get(Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        queryType = "select";
        changed = true;
        return executeQuery(model);
    }
    public <T extends DBModel> boolean update(Map<String, Object> update, Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        queryType = "update";
        changed = true;
        querySetColumns = update;
        return execute(model);
    }
    public <T extends DBModel> boolean delete(Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        queryType = "delete";
        changed = true;
        return execute(model);
    }

    //============== Sending query to connector;
    protected <T extends DBModel> ResultSet<T> executeQuery(Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        return executeRawQuery(toSql(model), model);
    }
    protected <T extends DBModel> ResultSet<T> executeRawQuery(String query, Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        DB conn = getConnector();
        if(conn == null)
            throw new NullPointerException("Connector is null!");
        return getConnector().executeQuery(query, model);
    }
    protected <T extends DBModel> boolean execute(Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        return executeRaw(toSql(model), model);
    }
    protected <T extends DBModel> boolean executeRaw(String query, Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        DB conn = getConnector();
        if(conn == null)
            throw new NullPointerException("Connector is null!");
        return getConnector().execute(query, model);
    }


    //============== single element return queries
    public <T extends DBModel> T first(Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        int oldLimit = limit;
        this.limit(1);
        ResultSet<T> result = this.get(model);
        this.limit(oldLimit);
        if(result.size() > 0)
            return result.get(0);
        return null;
    }
    public <T extends DBModel> T firstOrFail(Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        T result = first(model);
        if(result == null)
            throw new SQLException("No model got from query");
        return result;
    }
    public <T extends DBModel> T find(Object id, Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        ResultSet<T> result = executeRawQuery(buildFindQuery(id, model), model);
        if(result == null)
            throw new SQLException("No model got from query");
        if(result.size() > 0)
            return result.get(0);
        return null;
    }
    public <T extends DBModel> T findOrFail(Object id, Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        T result = find(id, model);
        if(result == null)
            throw new SQLException("No model with id " + id.toString());
        return result;
    }





    //===================  filling query
    public QueryBuilder select(String... columns){
        this.columns = Arrays.asList(columns);
        changed = true;
        return this;
    }



    public QueryBuilder where(String column, DBWhereOp op, Object value, boolean and){
        DBQueryWhere wh = new DBQueryWhere(column, op, value, and);
        currentWhereStack.peek().addChildren(wh);
        changed = true;
        return this;
    }
    public QueryBuilder where(String column, DBWhereOp op, Object value){
        return where(column, op, value, true);
    }
    public QueryBuilder where(String column, Object value, boolean and){
        return where(column, DBWhereOp.EQ, value, and);
    }
    public QueryBuilder where(String column, Object value){
        return where(column, DBWhereOp.EQ, value, true);
    }
    public QueryBuilder where(DBQueryWhere.WhereCallback callback, boolean and){
        if(callback == null)
            return this;
        DBQueryWhere wh = new DBQueryWhere(this, and);
        currentWhereStack.push(wh);
        callback.action(this);
        currentWhereStack.pop();
        currentWhereStack.peek().addChildren(wh);
        changed = true;
        return this;
    }
    public QueryBuilder where(DBQueryWhere.WhereCallback callback){
        return where(callback, true);
    }

    public QueryBuilder orWhere(String column, Object value){
        return where(column, DBWhereOp.EQ, value, false);
    }
    public QueryBuilder orWhere(String column, DBWhereOp op, Object value){
        return where(column, op, value, false);
    }
    public QueryBuilder orWhere(DBQueryWhere.WhereCallback callback){
        return where(callback, false);
    }

    public QueryBuilder limit (int limit){
        this.limit = limit;
        changed = true;
        return this;
    }


}
