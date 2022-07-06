package com.bedivierre.eloquent;

import com.bedivierre.eloquent.expr.DBQueryWhere;
import com.bedivierre.eloquent.expr.DBWhereOp;
import com.bedivierre.eloquent.model.DBModel;
import org.apache.commons.lang.StringUtils;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

/*********************************
 ** Code by Bedivierre
 ** 01.07.2022 14:10
 **********************************/
public class DB implements Closeable {
    private Connection connection;
    private String host;
    private int port;
    private String username;
    private String password;
    private String database;


    public String getDatabase(){return database;}
    public String getHost(){return host;}
    public int getPort(){return port;}

    public DB(String host, String database, String username, String password){
        setConnectionUri(host, database);
        setAuth(username, password);
    }
    public DB(String host, String database, String username, String password, int port){
        this(host, database, username, password);
        setPort(port);
    }
    protected void finalize(){
        close();
    }
    public void close(){
        try {
            if (connection != null)
                connection.close();
        } catch (Exception ex){

        }
    }

    public DB setDatabase(String database){this.database = database; return this;}
    public DB setHost(String host){this.host = host; return this;}
    public DB setConnectionUri(String host, String database){
        setHost(host).setDatabase(database);
        return this;
    }
    public DB setConnectionUri(String host, String database, int port){
        setHost(host).setDatabase(database).setPort(port);
        return this;
    }
    public DB setUserName(String username){this.username = username; return this;}
    public DB setPassword(String password){this.password = password; return this;}
    public DB setAuth(String username, String password){
        setUserName(username).setPassword(password);
        return this;
    }
    public DB setPort(int port){this.port = port; return this;}


    public boolean isConnected(){
        try {
            return connection != null && connection.isValid(1);
        } catch (Exception ex){
            return false;
        }
    }

    public Connection connect() throws SQLException, IOException {
        String url = "jdbc:mysql://" + host + "/" + database;
        connection = DriverManager.getConnection(url, username, password);
        return connection;
    }

    public <T extends DBModel> ResultSet<T> executeQuery(QueryBuilder query, Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        return executeQuery(query.toSql(model), model);
    }
    public <T extends DBModel> ResultSet<T> executeQuery(String query, Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        ResultSet<T> list = new ResultSet<>();
        Connection conn = null;
        if(!isConnected())
            conn = connect();
        if(!isConnected() || conn == null)
            throw new SQLException("mysql is not connected");
        java.sql.ResultSet set =  conn.createStatement().executeQuery(query);

        list = translateSqlResponseToModels(set, model);
        conn.close();
        return list;
    }
    public <T extends DBModel> boolean execute(QueryBuilder query, Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        return execute(query.toSql(model), model);
    }
    public <T extends DBModel> boolean execute(String query, Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        Connection conn = null;
        System.out.println(query);
        if(!isConnected())
            conn = connect();
        if(!isConnected() || conn == null)
            throw new SQLException("mysql is not connected");
        boolean result =  conn.createStatement().execute(query);
        conn.close();
        return result;
    }
    public <T extends DBModel> double executeAggregate(QueryBuilder query, Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        return executeAggregate(query.toSql(model), model);
    }
    public <T extends DBModel> double executeAggregate(String query, Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        Connection conn = null;

        if(!isConnected())
            conn = connect();
        if(!isConnected() || conn == null)
            throw new SQLException("mysql is not connected");
        java.sql.ResultSet result = conn.createStatement().executeQuery(query);
        double res = result.next() ? result.getDouble(1): 0;
        conn.close();
        return res;
    }
    
    private <T extends DBModel> ResultSet<T>
        translateSqlResponseToModels(java.sql.ResultSet sqlResp, Class<T> model)
            throws SQLException, InstantiationException, IllegalAccessException {
        ResultSet<T> list = new ResultSet<>();
        if(model == null)
            return list;
        while(sqlResp.next()){
            Field[] flist = model.getDeclaredFields();
            ArrayList<Field> forFill = new ArrayList<>();
            for (Field field : flist) {
                if(!StringUtils.startsWith(field.getName(), "__")){
                    forFill.add(field);
                }
            }
            T instance = createModel(sqlResp, forFill, model);
            list.add(instance);
        }
        return list;
    }
    private <T extends DBModel> T createModel(java.sql.ResultSet sqlResp, ArrayList<Field> fields, Class<T> model)
            throws InstantiationException, IllegalAccessException, SQLException {
        T instance = model.newInstance();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                if(field.getType().equals(String.class)){
                    field.set(instance, sqlResp.getString(field.getName()));
                }
                if(field.getType().equals(int.class)){
                    field.set(instance, sqlResp.getInt(field.getName()));
                }
                if(field.getType().equals(long.class)){
                    field.set(instance, sqlResp.getLong(field.getName()));
                }
                if(field.getType().equals(float.class)){
                    field.set(instance, sqlResp.getFloat(field.getName()));
                }
                if(field.getType().equals(double.class)){
                    field.set(instance, sqlResp.getDouble(field.getName()));
                }
                if(field.getType().equals(boolean.class)){
                    field.set(instance, sqlResp.getInt(field.getName()) != 0 );
                }
            } catch (SQLException ex){

            }
        }
        return instance;
    }


    public QueryBuilder query(){
        return new QueryBuilder(this);
    }

    // ==== wrappers for queries
    public <T extends DBModel> ResultSet<T> get(Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        return query().get(model);
    }
    public <T extends DBModel> void update(Map<String, Object> update, Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        query().update(update, model);
    }
    public <T extends DBModel> void insert(Map<String, Object> update, Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        query().insert(update, model);
    }
    public <T extends DBModel> void delete(Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        query().delete(model);
    }
    public <T extends DBModel> T find(Object id, Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        return query().find(id, model);
    }
    public <T extends DBModel> T first(Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        return query().first(model);
    }
    public <T extends DBModel> int count(Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        return query().count(model);
    }
    public <T extends DBModel> double min(String column, Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        return query().min(column, model);
    }
    public <T extends DBModel> double max(String column, Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        return query().max(column, model);
    }
    public <T extends DBModel> double avg(String column, Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        return query().avg(column, model);
    }
    public <T extends DBModel> double sum(String column, Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        return query().sum(column, model);
    }

    //==== wrappers for queryBuilder
    public QueryBuilder where(DBQueryWhere.WhereCallback callback){
        return query().where(callback);
    }
    public QueryBuilder where(String column, DBWhereOp op, Object value){
        return query().where(column, op, value);
    }
    public QueryBuilder where(String column, Object value){
        return query().where(column, value);
    }

}
