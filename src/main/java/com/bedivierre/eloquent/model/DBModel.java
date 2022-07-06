package com.bedivierre.eloquent.model;

import com.bedivierre.eloquent.DB;
import com.bedivierre.eloquent.QueryBuilder;

import java.lang.invoke.MethodHandles;

/*********************************
 ** Code by Bedivierre
 ** 01.07.2022 14:23
 **********************************/
public abstract class DBModel {
    public String getTable() {return "";}
    public String getPrimaryKey(){return "id";}


}
