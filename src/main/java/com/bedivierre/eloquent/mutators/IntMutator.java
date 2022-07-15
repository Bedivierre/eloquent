package com.bedivierre.eloquent.mutators;

/*********************************
 ** Code by Bedivierre
 ** 15.07.2022 12:02
 **********************************/
public class IntMutator extends TypeMutator<Integer>{
    @Override
    public Integer mutate(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ex){
            return 0;
        }
    }
}
