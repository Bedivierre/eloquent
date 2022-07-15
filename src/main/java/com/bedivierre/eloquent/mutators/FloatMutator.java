package com.bedivierre.eloquent.mutators;

/*********************************
 ** Code by Bedivierre
 ** 15.07.2022 12:02
 **********************************/
public class FloatMutator extends TypeMutator<Float>{
    @Override
    public Float mutate(String value) {
        try {
            return Float.parseFloat(value);
        } catch (Exception ex){
            return 0f;
        }
    }
}
