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
    private final DBQueryWhere<T> parentWhere;
    private boolean distinct = false;
    private final Stack<DBQueryWhere<T>> currentWhereStack;
    private Map<String, Object> querySetColumns;
    String queryText = "";
    String aggregateColumn = "";
    private int limit = -1;
    private Object findId;
    private final Class<T> model;


    public Class<T> getModel(){return model;}
    public boolean isChanged(){return changed;}
    public boolean isDistinct(){return distinct;}
    public List<String> getColumns() {return columns;};
    public List<String> getOrderByColumns() {return orderByColumns;}
    public boolean isOrderByAsc(){return orderByAsc;}
    public Object getFindId() {return findId;}
    public String getQueryType(){return StringUtils.lowerCase(queryType);}
    public DB getConnector() {return connector;}
    public DBQueryWhere<T> getParentWhere() {return parentWhere;}
    public void setConnector(DB conn) {connector = conn;}
    public int getLimit(){return limit;}
    public String getAggregateColumn() { return aggregateColumn;}
    public Map<String, Object> getQuerySetColumns() { return querySetColumns;};

    public QueryBuilder(DB connector, Class<T> model){
        setConnector(connector);
        this.model = model;


        columns = new ArrayList<>();
        columns.add("*");
        parentWhere = new DBQueryWhere<T>(this);
        currentWhereStack = new Stack<>();
        currentWhereStack.push(parentWhere);
    }

    public String toSql()
            throws InstantiationException, IllegalAccessException, SQLException {
        return getConnector().getDialect().buildQuery(this);
    }

    //============== query toSql builders

    public T getModelInstance()
            throws InstantiationException, IllegalAccessException, SQLException {
        return model.newInstance();
    }

    //============== common queries
    public ResultSet<T> get()
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        queryType = "select";
        changed = true;
        return getConnector().executeQuery(this);
    }
    public boolean update(Map<String, Object> update)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        queryType = "update";
        changed = true;
        querySetColumns = update;
        return getConnector().execute(this);
    }

    public boolean insert(Map<String, Object> insert)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        queryType = "insert";
        changed = true;
        querySetColumns = insert;
        return getConnector().execute(this);
    }
    public boolean delete()
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        queryType = "delete";
        changed = true;
        return getConnector().execute(this);
    }


    //============== Sending query to connector;
    protected ResultSet<T> executeQuery()
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        if(getConnector() == null)
            throw new NullPointerException("Connector is null!");
        return getConnector().executeQuery(this);
    }


    //============== single element return queries
    public T first()
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        int oldLimit = limit;
        this.limit(1);
        ResultSet<T> result = this.get();
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
        findId = id;
        queryType = "find";
        ResultSet<T> result = getConnector().executeQuery(this);
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
        return (int)getConnector().executeAggregate(this);
    }
    public double max(String column)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        queryType = "max";
        changed = true;
        aggregateColumn = column;
        return getConnector().executeAggregate(this);
    }
    public double min(String column)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        queryType = "min";
        changed = true;
        aggregateColumn = column;
        return getConnector().executeAggregate(this);
    }
    public double avg(String column)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        queryType = "avg";
        changed = true;
        aggregateColumn = column;
        return getConnector().executeAggregate(this);
    }
    public double sum(String column)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        queryType = "sum";
        changed = true;
        aggregateColumn = column;
        return getConnector().executeAggregate(this);
    }



    //===================  filling query
    public QueryBuilder<T> select(String... columns){
        this.columns = Arrays.asList(columns);
        changed = true;
        return this;
    }



    public QueryBuilder<T> where(String column, DBWhereOp op, Object value, boolean and, boolean not){
        DBQueryWhere<T> wh = new DBQueryWhere<T>(column, op, value, and, not);
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
    public QueryBuilder<T> where(DBQueryWhere.WhereCallback<T> callback, boolean and, boolean not){
        if(callback == null)
            return this;
        DBQueryWhere<T> wh = new DBQueryWhere<T>(this, and, not);
        currentWhereStack.push(wh);
        callback.action(this);
        currentWhereStack.pop();
        currentWhereStack.peek().addChildren(wh);
        changed = true;
        return this;
    }
    public QueryBuilder<T> where(DBQueryWhere.WhereCallback<T> callback, boolean and){
        return where(callback, and, false);
    }
    public QueryBuilder<T> where(DBQueryWhere.WhereCallback<T> callback){
        return where(callback, true);
    }

    public QueryBuilder<T> whereNot(DBQueryWhere.WhereCallback<T> callback){
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
    public QueryBuilder<T> orWhere(DBQueryWhere.WhereCallback<T> callback){
        return where(callback, false);
    }
    public QueryBuilder<T> orWhereNot(String column, Object value){
        return where(column, DBWhereOp.EQ, value, false, true);
    }
    public QueryBuilder<T> orWhereNot(String column, DBWhereOp op, Object value){
        return where(column, op, value, false, true);
    }
    public QueryBuilder<T> orWhereNot(DBQueryWhere.WhereCallback<T> callback){
        return where(callback, false, true);
    }


    public QueryBuilder<T> whereIn(String column, boolean and, boolean not, Object... values){
        if(values.length == 0)
            return this;
        DBQueryWhere<T> wh = new DBQueryWhere<T>(column, DBWhereOp.IN, values, and, not);
        currentWhereStack.peek().addChildren(wh);
        changed = true;
        return this;
    }
    public QueryBuilder<T> whereIn(String column, boolean and, Object... values){
        return whereIn(column, and, false, values);
    }
    public QueryBuilder<T> whereIn(String column, Object... values){
        return whereIn(column, true, false, values);
    }
    public QueryBuilder<T> whereNotIn(String column, Object... values){
        return whereIn(column, true, true, values);
    }
    public QueryBuilder<T> whereNotIn(String column, boolean and, Object... values){
        return whereIn(column, and, true, values);
    }
    public QueryBuilder<T> orWhereIn(String column, boolean not, Object... values){
        return whereIn(column, false, not, values);
    }
    public QueryBuilder<T> orWhereIn(String column, Object... values){
        return whereIn(column, false, false, values);
    }
    public QueryBuilder<T> orWhereNotIn(String column, Object... values){
        return whereIn(column, false, true, values);
    }



    public QueryBuilder<T> whereBetween(String column, boolean and, boolean not, Object value1, Object value2){

        DBQueryWhere<T> wh = new DBQueryWhere<T>(column, DBWhereOp.BETWEEN, Arrays.asList(value1, value2), and, not);
        currentWhereStack.peek().addChildren(wh);
        changed = true;
        return this;
    }
    public QueryBuilder<T> whereBetween(String column, boolean and, Object value1, Object value2){
        return whereBetween(column, and, false, value1, value2);
    }
    public QueryBuilder<T> whereBetween(String column, Object value1, Object value2){
        return whereBetween(column, true, false, value1, value2);
    }
    public QueryBuilder<T> whereNotBetween(String column, boolean and, Object value1, Object value2){
        return whereBetween(column, and, true, value1, value2);
    }
    public QueryBuilder<T> whereNotBetween(String column, Object value1, Object value2){
        return whereBetween(column, true, true, value1, value2);
    }
    public QueryBuilder<T> orWhereBetween(String column, Object value1, Object value2){
        return whereBetween(column, false, false, value1, value2);
    }
    public QueryBuilder<T> orWhereNotBetween(String column, Object value1, Object value2){
        return whereBetween(column, false, true, value1, value2);
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
