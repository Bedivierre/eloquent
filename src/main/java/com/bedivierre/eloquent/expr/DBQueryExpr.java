package com.bedivierre.eloquent.expr;

import com.bedivierre.eloquent.DB;
import com.bedivierre.eloquent.QueryBuilder;
import com.bedivierre.eloquent.model.DBModel;

import java.util.ArrayList;
import java.util.List;

/*********************************
 ** Code by Bedivierre
 ** 04.07.2022 11:03
 **********************************/
public abstract class DBQueryExpr<T extends DBModel> {
    protected List<DBQueryExpr<T>> children = new ArrayList<>();
    protected DBQueryExpr<T> parent;
    protected QueryBuilder<T> query;

    public abstract String toSql();
    public List<DBQueryExpr<T>> getChildren() {return children;}
    public DBQueryExpr<T> getParent() {return parent;}
    public QueryBuilder<T> getQuery() {return query;}
    public DB getConnector() {return query != null ? query.getConnector() : null;}
    public void setQuery(QueryBuilder<T> query) {
        this.query = query;
        for (DBQueryExpr<T> expr: getChildren()) {
            expr.setQuery(query);
        }
    }



    public void addChildren(DBQueryExpr<T> expr){
        children.add(expr);
        expr.parent = this;
        setQuery(this.query);
    }

    protected DBQueryExpr(){
        this(null, null);
    }
    protected DBQueryExpr(DBQueryExpr<T> parent){
        this(parent, parent == null ? null : parent.query);
    }
    protected DBQueryExpr(QueryBuilder<T> query){
        this(null, query);
    }
    protected DBQueryExpr(DBQueryExpr<T> parent, QueryBuilder<T> query){
        this.parent = parent;
        setQuery(query);
    }
}
