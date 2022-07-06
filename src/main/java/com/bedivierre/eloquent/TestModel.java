package com.bedivierre.eloquent;

import com.bedivierre.eloquent.model.DBModel;

/*********************************
 ** Code by Bedivierre
 ** 01.07.2022 15:40
 **********************************/
public class TestModel extends DBModel {

    @Override
    public String getTable(){return "gc_request";}

    public int id;
    public int type;
    public int time;
    public boolean mode;
    public String credentials;
}
