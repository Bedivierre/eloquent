package com.bedivierre.eloquent.expr;

import com.bedivierre.eloquent.QueryBuilder;
import com.bedivierre.eloquent.utils.Util;

/*********************************
 ** Code by Bedivierre
 ** 04.07.2022 11:01
 **********************************/
public class DBQueryWhere extends DBQueryExpr {
    public interface WhereCallback {
        void action(QueryBuilder query);
    }

    private boolean and = true;
    private boolean not = false;
    private DBWhereOp op = DBWhereOp.EQ;
    private String column = "";
    private Object value = "";

    public boolean isAnd() {return and;}
    public boolean isNot() {return not;}
    public DBWhereOp getOp() {return op;}
    public String getOpString() {return op.value;}
    public String getColumn() {return column;}
    public Object getValue() {return value;}
    public String getValueString() {return value.toString();}

    public DBQueryWhere(String column, DBWhereOp op, Object value, boolean and, boolean not){
        super();
        setParams(column, op, value, and, not);
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
    public DBQueryWhere(QueryBuilder query){
        super(query);
        setParams("", DBWhereOp.EQ, "", true);
    }
    public DBQueryWhere(QueryBuilder query, boolean and){
        super(query);
        setParams("", DBWhereOp.EQ, "", and);
    }
    public DBQueryWhere(QueryBuilder query, boolean and, boolean not){
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
    public void setParams(String column, DBWhereOp op, Object value, boolean and){
        setParams(column, op, value, and, false);
    }

    @Override
    public String toSql() {
        StringBuilder b = new StringBuilder();

        if(getChildren().size() > 0) {
            for (int i = 0; i < getChildren().size(); i++) {
                DBQueryWhere wh = getChildren().get(i) instanceof DBQueryWhere
                        ? (DBQueryWhere)getChildren().get(i) : null;
                if(wh == null)
                    continue;

                if(i != 0 )
                    b.append(wh.isAnd() ? " AND " : " OR ");

                if(isNot())
                    b.append("NOT ");
                if(wh.getChildren().size() > 0)
                    b.append('(');
                b.append(wh.toSql());
                if(wh.getChildren().size() > 0)
                    b.append(')');
            }
        } else {

            if(isNot())
                b.append("NOT ");
            b.append(Util.processIdentifier(getColumn()));
            b.append(' ').append(getOpString()).append(' ');
            b.append(Util.processValue(getValue()));
        }

        return b.toString();
    }
}
