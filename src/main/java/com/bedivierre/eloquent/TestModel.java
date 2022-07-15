package com.bedivierre.eloquent;

import com.bedivierre.eloquent.annotations.SqlField;
import com.bedivierre.eloquent.annotations.SqlFieldType;
import com.bedivierre.eloquent.model.DBModel;

import java.util.ArrayList;

/*********************************
 ** Code by Bedivierre
 ** 01.07.2022 15:40
 **********************************/
public class TestModel extends DBModel {

    @Override
    public String getTable(){return "gc_request";}

    @SqlField(column = "id")
    public int id;
    public int type;
    public int time;
    public boolean mode;
    public String credentials;

    @SqlField(type = SqlFieldType.ARRAY, arrayElementType = {String.class})
    public ArrayList<String> address;
}
