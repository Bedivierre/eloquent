package com.bedivierre.eloquent.mutators;

/*********************************
 ** Code by Bedivierre
 ** 15.07.2022 12:02
 **********************************/
public class DoubleMutator extends TypeMutator<Double>{
    @Override
    public Double mutate(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception ex){
            return 0.0;
        }
    }
}
