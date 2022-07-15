package com.bedivierre.eloquent;

import com.bedivierre.eloquent.dialects.MySQLDialect;
import com.bedivierre.eloquent.dialects.SQLDialect;
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
    public final static SQLDialect defaultDialect = new MySQLDialect();

    private Connection connection;
    private String host;
    private int port;
    private String username;
    private String password;
    private String database;
    private SQLDialect dialect;


    public String getDatabase(){return database;}
    public String getHost(){return host;}
    public int getPort(){return port;}
    public SQLDialect getDialect() {return dialect;}

    public DB(String host, String database, String username, String password){
        this(host, database, username, password, new MySQLDialect());
    }
    public DB(String host, String database, String username, String password, SQLDialect dialect){
        setConnectionUri(host, database);
        setAuth(username, password);
        this.dialect = dialect == null ? defaultDialect : dialect;

    }
    public DB(String host, String database, String username, String password, int port){
        this(host, database, username, password);
        setPort(port);
    }
    public DB(String host, String database, String username, String password, int port, SQLDialect dialect){
        this(host, database, username, password, dialect);
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

    public java.sql.ResultSet executeRaw(String query)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        Connection conn = null;
        if(!isConnected())
            conn = connect();
        if(!isConnected() || conn == null)
            throw new SQLException("mysql is not connected");
        return conn.createStatement().executeQuery(query);
    }
    public <T extends DBModel> ResultSet<T> executeQuery(QueryBuilder<T> query)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        ResultSet<T> list;
        Connection conn = null;
        if(!isConnected())
            conn = connect();
        if(!isConnected() || conn == null)
            throw new SQLException("mysql is not connected");
        java.sql.ResultSet set =  conn.createStatement().executeQuery(getDialect().buildQuery(query));

        list = (new DBResolver()).sqlToModels(set, query.getModel());
        conn.close();
        return list;
    }
    public <T extends DBModel> boolean execute(QueryBuilder<T> query)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        Connection conn = null;
        System.out.println(query);
        if(!isConnected())
            conn = connect();
        if(!isConnected() || conn == null)
            throw new SQLException("mysql is not connected");
        boolean result =  conn.createStatement().execute(getDialect().buildQuery(query));
        conn.close();
        return result;
    }
    public <T extends DBModel> double executeAggregate(QueryBuilder<T> query)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        Connection conn = null;
        if(!isConnected())
            conn = connect();
        if(!isConnected() || conn == null)
            throw new SQLException("mysql is not connected");
        java.sql.ResultSet result = conn.createStatement().executeQuery(getDialect().buildQuery(query));
        double res = result.next() ? result.getDouble(1): 0;
        conn.close();
        return res;
    }
    



    public <T extends DBModel> QueryBuilder<T> query(Class<T> model){
        return new QueryBuilder<T>(this, model);
    }

    // ==== wrappers for queries
    public <T extends DBModel> ResultSet<T> get(Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        return query(model).get();
    }
    public <T extends DBModel> void update(Class<T> model, Map<String, Object> update)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        query(model).update(update);
    }
    public <T extends DBModel> void insert(Class<T> model, Map<String, Object> update)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        query(model).insert(update);
    }
    public <T extends DBModel> void delete(Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        query(model).delete();
    }
    public <T extends DBModel> T find(Object id, Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        return query(model).find(id);
    }
    public <T extends DBModel> T first(Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        return query(model).first();
    }
    public <T extends DBModel> int count(Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        return query(model).count();
    }
    public <T extends DBModel> double min(String column, Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        return query(model).min(column);
    }
    public <T extends DBModel> double max(String column, Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        return query(model).max(column);
    }
    public <T extends DBModel> double avg(String column, Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        return query(model).avg(column);
    }
    public <T extends DBModel> double sum(String column, Class<T> model)
            throws SQLException, IOException, InstantiationException, IllegalAccessException {
        return query(model).sum(column);
    }

    //==== wrappers for queryBuilder
    public <T extends DBModel> QueryBuilder<T> where(Class<T> model, DBQueryWhere.WhereCallback<T> callback){
        return query(model).where(callback);
    }
    public <T extends DBModel> QueryBuilder<T> where(Class<T> model, String column, DBWhereOp op, Object value){
        return query(model).where(column, op, value);
    }
    public <T extends DBModel> QueryBuilder<T> where(Class<T> model, String column, Object value){
        return query(model).where(column, value);
    }
    public <T extends DBModel> QueryBuilder<T> whereIn(Class<T> model, String column, Object... values){
        return query(model).whereIn(column, values);
    }
    public <T extends DBModel> QueryBuilder<T> whereNotIn(Class<T> model, String column, Object... values){
        return query(model).whereNotIn(column, values);
    }
    public <T extends DBModel> QueryBuilder<T> whereBetween(Class<T> model, String column, Object value1, Object value2){
        return query(model).whereBetween(column, value1, value2);
    }
    public <T extends DBModel> QueryBuilder<T> whereNotBetween(Class<T> model, String column, Object value1, Object value2){
        return query(model).whereNotBetween(column, value1, value2);
    }

}
