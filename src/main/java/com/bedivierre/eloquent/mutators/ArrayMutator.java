package com.bedivierre.eloquent.mutators;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*********************************
 ** Code by Bedivierre
 ** 15.07.2022 12:02
 **********************************/
public class ArrayMutator<T> extends TypeMutator<T>{

    public List<T> mutateArray(String value, Class<T> model) {
        try {
            ArrayList<T> l = new ArrayList<>();
            Gson gson = new Gson();
            l = gson.fromJson(value, l.getClass());

            return l;
        } catch (Exception ex){
            return new ArrayList<>();
        }
    }

    @Override
    public String arrayToSql(List<T> data){
        Gson gson = new Gson();
        return gson.toJson(data, data.getClass());
    }
}
