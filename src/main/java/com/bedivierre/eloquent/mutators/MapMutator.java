package com.bedivierre.eloquent.mutators;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*********************************
 ** Code by Bedivierre
 ** 15.07.2022 12:02
 **********************************/
public class MapMutator<T> extends TypeMutator<T>{

    public Map<String, T> mutateMap(String value, Class<T> model) {
        try {
            HashMap<String, T> l = new HashMap<>();
            Gson gson = new Gson();
            l = gson.fromJson(value, l.getClass());
            return l;
        } catch (Exception ex){
            return new HashMap<>();
        }
    }

    @Override
    public String mapToSql(Map<String, T> data){
        Gson gson = new Gson();
        return gson.toJson(data, data.getClass());
    }
}
