package com.bedivierre.eloquent.expr;

import com.bedivierre.eloquent.QueryBuilder;
import com.bedivierre.eloquent.model.DBModel;
import com.bedivierre.eloquent.utils.Util;

/*********************************
 ** Code by Bedivierre
 ** 04.07.2022 11:01
 **********************************/
public class  DBQueryWhere<T extends DBModel> extends DBQueryExpr<T> {
    public interface WhereCallback<T extends DBModel> {
        void action(QueryBuilder<T> query);
    }

    private boolean and = true;
    private boolean not = false;
    private DBWhereOp op = DBWhereOp.EQ;
    private String column = "";
    private Object value = "";
    private Object[] values;

    public boolean isAnd() {return and;}
    public boolean isNot() {return not;}
    public DBWhereOp getOp() {return op;}
    public String getOpString() {return op.value;}
    public String getColumn() {return column;}
    public Object getValue() {return value;}
    public Object[] getValues() {return values;}
    public String getValueString() {return value.toString();}

    public DBQueryWhere(String column, DBWhereOp op, Object value, boolean and, boolean not){
        super();
        setParams(column, op, value, and, not);
    }

    public DBQueryWhere(String column, DBWhereOp op, Object[] valueArr, boolean and, boolean not){
        super();
        setParams(column, op, valueArr, and, not);
    }
    public DBQueryWhere(String column, DBWhereOp op, Object value, boolean and){
        super();
        setParams(column, op, value, and, false);
    }
    public DBQueryWhere(String column, Object value, boolean and){
        super();
        setParams(column, DBWhereOp.EQ, value, and);
    }
    public DBQueryWhere(String column, DBWhereOp op, Object value){
        super();
        setParams(column, op, value, true);
    }
    public DBQueryWhere(String column, Object value){
        super();
        setParams(column, DBWhereOp.EQ, value, true);
    }
    public DBQueryWhere(QueryBuilder<T> query){
        super(query);
        setParams("", DBWhereOp.EQ, "", true);
    }
    public DBQueryWhere(QueryBuilder<T> query, boolean and){
        super(query);
        setParams("", DBWhereOp.EQ, "", and);
    }
    public DBQueryWhere(QueryBuilder<T> query, boolean and, boolean not){
        super(query);
        setParams("", DBWhereOp.EQ, "", and, not);
    }
    public DBQueryWhere(){
        super();
        setParams("", DBWhereOp.EQ, "", true);
    }

    public void setParams(String column, DBWhereOp op, Object value, boolean and, boolean not){
        this.column = column;
        this.op = op;
        this.value = value;
        this.and = and;
        this.not = not;
    }

    public void setParams(String column, DBWhereOp op, Object[] value, boolean and, boolean not){
        this.column = column;
        this.op = op;
        this.values = value;
        this.and = and;
        this.not = not;
    }
    public void setParams(String column, DBWhereOp op, Object value, boolean and){
        setParams(column, op, value, and, false);
    }

    @Override
    public String toSql() {
        return getConnector().getDialect().whereExpr(this);
    }
}
