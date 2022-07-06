package com.bedivierre.eloquent.expr;

import com.bedivierre.eloquent.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

/*********************************
 ** Code by Bedivierre
 ** 04.07.2022 11:03
 **********************************/
public abstract class DBQueryExpr {
    protected List<DBQueryExpr> children = new ArrayList<>();
    protected DBQueryExpr parent;
    protected QueryBuilder query;

    public abstract String toSql();
    public List<DBQueryExpr> getChildren() {return children;}
    public DBQueryExpr getParent() {return parent;}
    public QueryBuilder getQuery() {return query;}
    public void setQuery(QueryBuilder query) {
        this.query = query;
        for (DBQueryExpr expr: getChildren()) {
            expr.setQuery(query);
        }
    }



    public void addChildren(DBQueryExpr expr){
        children.add(expr);
        expr.parent = this;
        setQuery(this.query);
    }

    protected DBQueryExpr(){
        this(null, null);
    }
    protected DBQueryExpr(DBQueryExpr parent){
        this(parent, parent == null ? null : parent.query);
    }
    protected DBQueryExpr(QueryBuilder query){
        this(null, query);
    }
    protected DBQueryExpr(DBQueryExpr parent, QueryBuilder query){
        this.parent = parent;
        setQuery(query);
    }
}
