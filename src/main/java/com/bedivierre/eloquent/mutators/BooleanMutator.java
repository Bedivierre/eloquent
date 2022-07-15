package com.bedivierre.eloquent.mutators;

import org.apache.commons.lang.StringUtils;

/*********************************
 ** Code by Bedivierre
 ** 15.07.2022 12:02
 **********************************/
public class BooleanMutator extends TypeMutator<Boolean>{
    @Override
    public Boolean mutate(String value) {
        try {
            String v = StringUtils.trim(value);
            return v != null && !StringUtils.isEmpty(v) && !v.equals("0") && !StringUtils.lowerCase(v).equals("false");
        } catch (Exception ex){
            return false;
        }
    }
}
