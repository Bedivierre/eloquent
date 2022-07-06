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
public class QueryBuilder <T extends DBModel> {


    private DB connector;
    private List<String> columns;
    private List<String> orderByColumns;
    private boolean orderByAsc;
    private String queryType = "select";
    private boolean changed = true;
    private DBQueryWhere parentWhere;
    private boolean distinct = false;
    private Stack<DBQueryWhere> currentWhereStack;
    private Map<String, Object> querySetColumns;
    String queryText = "";
    String aggregateColumn = "";
    private int limit = -1;
    private final Class<T> model;


    public Class<T> getModel(){return model;}
    private boolean isChanged(){return changed;}
    public String getQueryType(){return StringUtils.lowerCase(queryType);}
    public DB getConnector() {return connector;}
    public DBQueryWhere getParentWhere() {return parentWhere;}
    public void setConnector(DB conn) {connector = conn;}

    public QueryBuilder(DB connector, Class<T> model){
        setConnector(connector);
        this.model = model;


        columns = new ArrayList<>();
        columns.add("*");
        parentWhere = new DBQueryWhere(this);
        currentWhereStack = new Stack<>();
        currentWhereStack.push(parentWhere);
    }

    public String toSql()
            throws InstantiationException, IllegalAccessException, SQLException {
        return buildQuery();
    }
    private String buildQuery()
            throws InstantiationException, IllegalAccessException, SQLException {
        if(!isChanged())
            return queryText;
        if(getQueryType().equals("select"))
            queryText = buildSelectQuery();
        if(getQueryType().equals("insert"))
            queryText = buildInsertQuery();
        if(getQueryType().equals("update"))
            queryText = buildUpdateQuery();
        if(getQueryType().equals("delete"))
            queryText = buildDeleteQuery();
        if(getQueryType().equals("count") || getQueryType().equals("min") || getQueryType().equals("max")
                || getQueryType().equals("avg") || getQueryType().equals("sum"))
            queryText = buildAggregateQuery(getQueryType(), aggregateColumn);
        changed = false;
        return queryText;
    }


    //============== query toSql builders
    private String buildFindQuery(Object id)
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
    private String buildSelectQuery()
            throws InstantiationException, IllegalAccessException, SQLException {

        T instance = model.newInstance();
        String table = instance.getTable();
        if(table.length() == 0){
            throw new SQLException("Table is not defined!");
        }

        StringBuilder b = new StringBuilder();
        b.append("SELECT ");
        if(distinct)
            b.append("DISTINCT ");
        b.append(processColumns()).append(" FROM ");
        b.append(Util.processIdentifier(table));

        if(getParentWhere().getChildren().size() > 0){
            b.append(" WHERE ").append(getParentWhere().toSql());
        }
        if(limit > 0){
            b.append(" LIMIT ").append(limit);
        }
        b.append(processOrderBy());

        b.append(";");
        return b.toString();
    }
    private String buildUpdateQuery()
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
    private String buildInsertQuery()
            throws InstantiationException, IllegalAccessException, SQLException {
        T instance = model.newInstance();
        String table = instance.getTable();
        if(table.length() == 0){
            throw new SQLException("Table is not defined!");
        }
        if(querySetColumns == null || querySetColumns.size() == 0)
            throw new SQLException("Insert parameters is not defined!");

        StringBuilder b = new StringBuilder();
        b.append("INSERT INTO ").append(Util.processIdentifier(table));

        b.append("(");
        List<String> keys = Arrays.asList(querySetColumns.keySet().toArray(new String[0]));
        for(int i = 0; i < keys.size(); i++){
            b.append(Util.processIdentifier(keys.get(i)));
            if(i < keys.size() - 1)
                b.append(", ");
        }
        b.append(")");
        b.append(" VALUES ");
        b.append("(");
        List<Object> values = Arrays.asList(querySetColumns.values().toArray());
        for(int i = 0; i < values.size(); i++){
            b.append(Util.processValue(values.get(i)));
            if(i < values.size() - 1)
                b.append(", ");
        }
        b.append(")");

        b.append(";");
        return b.toString();
    }
    private String buildDeleteQuery()
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
    private String buildAggregateQuery(String type, String column)
            throws InstantiationException, IllegalAccessException, SQLException {

        T instance = model.newInstance();
        String table = instance.getTable();
        if(table.length() == 0){
            throw new SQLException("Table is not defined!");
        }

        if(column == null || column.length() == 0)
            column = instance.getPrimaryKey();

        StringBuilder b = new StringBuilder();
        b.append("SELECT ").append(StringUtils.upperCase(type))
                .append("(").append(Util.processIdentifier(column)).append(") AS __number__");
        b.append(" FROM ");
        b.append(Util.processIdentifier(table));

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
    private String processOrderBy(){
        if(orderByColumns == null || orderByColumns.size() == 0)
            return "";
        StringBuilder b = new StringBuilder();
        for(int i = 0; i < orderByColumns.size(); i++){
            String col = orderByColumns.get(i);
            b.append(Util.processIdentifier(col));
            if(i < columns.size() - 1){
                b.append(",");
            }
        }
        b.append(orderByAsc ? " ASC" : " DESC");
        return b.toString();
    }


    //============== common queries
    public ResultSet<T> get(Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        queryType = "select";
        changed = true;
        return executeQuery();
    }
    public boolean update(Map<String, Object> update)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        queryType = "update";
        changed = true;
        querySetColumns = update;
        return execute();
    }

    public boolean insert(Map<String, Object> insert)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        queryType = "insert";
        changed = true;
        querySetColumns = insert;
        return execute();
    }
    public boolean delete()
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        queryType = "delete";
        changed = true;
        return execute();
    }


    //============== Sending query to connector;
    protected ResultSet<T> executeQuery()
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        if(getConnector() == null)
            throw new NullPointerException("Connector is null!");
        return getConnector().executeQuery(this);
    }
    protected ResultSet<T> executeRawQuery(String query)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        if(getConnector() == null)
            throw new NullPointerException("Connector is null!");
        return getConnector().executeQuery(query, model);
    }
    protected boolean execute()
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        return executeRaw(toSql());
    }
    protected boolean executeRaw(String query)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        if(getConnector() == null)
            throw new NullPointerException("Connector is null!");
        return getConnector().execute(query);
    }
    protected double executeAggregate()
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        return executeAggregateRaw(toSql());
    }
    protected double executeAggregateRaw(String query)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        if(getConnector() == null)
            throw new NullPointerException("Connector is null!");
        return getConnector().executeAggregate(query);
    }


    //============== single element return queries
    public T first()
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        int oldLimit = limit;
        this.limit(1);
        ResultSet<T> result = this.get(model);
        this.limit(oldLimit);
        if(result.size() > 0)
            return result.get(0);
        return null;
    }
    public T firstOrFail()
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        T result = first();
        if(result == null)
            throw new SQLException("No model got from query");
        return result;
    }
    public T find(Object id)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        ResultSet<T> result = executeRawQuery(buildFindQuery(id));
        if(result == null)
            throw new SQLException("No model got from query");
        if(result.size() > 0)
            return result.get(0);
        return null;
    }
    public T findOrFail(Object id)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        T result = find(id);
        if(result == null)
            throw new SQLException("No model with id " + id.toString());
        return result;
    }


    //=================== Aggregate functions

    public int count()
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        queryType = "count";
        changed = true;
        aggregateColumn = null;
        return (int)executeAggregate();
    }
    public double max(String column)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        queryType = "max";
        changed = true;
        aggregateColumn = column;
        return executeAggregate();
    }
    public double min(String column)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        queryType = "min";
        changed = true;
        aggregateColumn = column;
        return executeAggregate();
    }
    public double avg(String column)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        queryType = "avg";
        changed = true;
        aggregateColumn = column;
        return executeAggregate();
    }
    public double sum(String column)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        queryType = "sum";
        changed = true;
        aggregateColumn = column;
        return executeAggregate();
    }



    //===================  filling query
    public QueryBuilder<T> select(String... columns){
        this.columns = Arrays.asList(columns);
        changed = true;
        return this;
    }



    public QueryBuilder<T> where(String column, DBWhereOp op, Object value, boolean and, boolean not){
        DBQueryWhere wh = new DBQueryWhere(column, op, value, and, not);
        currentWhereStack.peek().addChildren(wh);
        changed = true;
        return this;
    }
    public QueryBuilder<T> where(String column, DBWhereOp op, Object value, boolean and){
        return where(column, op, value, and, false);
    }
    public QueryBuilder<T> where(String column, DBWhereOp op, Object value){
        return where(column, op, value, true);
    }
    public QueryBuilder<T> where(String column, Object value, boolean and){
        return where(column, DBWhereOp.EQ, value, and);
    }
    public QueryBuilder<T> where(String column, Object value){
        return where(column, DBWhereOp.EQ, value, true);
    }
    public QueryBuilder<T> where(DBQueryWhere.WhereCallback callback, boolean and, boolean not){
        if(callback == null)
            return this;
        DBQueryWhere wh = new DBQueryWhere(this, and, not);
        currentWhereStack.push(wh);
        callback.action(this);
        currentWhereStack.pop();
        currentWhereStack.peek().addChildren(wh);
        changed = true;
        return this;
    }
    public QueryBuilder<T> where(DBQueryWhere.WhereCallback callback, boolean and){
        return where(callback, and, false);
    }
    public QueryBuilder<T> where(DBQueryWhere.WhereCallback callback){
        return where(callback, true);
    }

    public QueryBuilder<T> whereNot(DBQueryWhere.WhereCallback callback){
        return where(callback, true, true);
    }
    public QueryBuilder<T> whereNot(String column, Object value){
        return where(column, DBWhereOp.EQ, value, true, true);
    }
    public QueryBuilder<T> whereNot(String column, DBWhereOp op, Object value){
        return where(column, op, value, true, true);
    }


    public QueryBuilder<T> orWhere(String column, Object value){
        return where(column, DBWhereOp.EQ, value, false);
    }
    public QueryBuilder<T> orWhere(String column, DBWhereOp op, Object value){
        return where(column, op, value, false);
    }
    public QueryBuilder<T> orWhere(DBQueryWhere.WhereCallback callback){
        return where(callback, false);
    }

    public QueryBuilder<T> orWhereNot(String column, Object value){
        return where(column, DBWhereOp.EQ, value, false, true);
    }
    public QueryBuilder<T> orWhereNot(String column, DBWhereOp op, Object value){
        return where(column, op, value, false, true);
    }
    public QueryBuilder<T> orWhereNot(DBQueryWhere.WhereCallback callback){
        return where(callback, false, true);
    }

    public QueryBuilder<T> limit (int limit){
        this.limit = limit;
        changed = true;
        return this;
    }
    public QueryBuilder<T> distinct (boolean value){
        this.distinct = value;
        changed = true;
        return this;
    }
    public QueryBuilder<T> distinct (){
        return distinct(true);
    }
    public QueryBuilder<T> orderBy(boolean asc, String... columns){
        orderByAsc = asc;
        orderByColumns = Arrays.asList(columns);
        changed = true;
        return this;
    }
    public QueryBuilder<T> orderBy(String... columns){
        return orderBy(true, columns);
    }

}
