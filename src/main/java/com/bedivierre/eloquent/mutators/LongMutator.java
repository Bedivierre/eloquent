package com.bedivierre.eloquent.mutators;

/*********************************
 ** Code by Bedivierre
 ** 15.07.2022 12:02
 **********************************/
public class LongMutator extends TypeMutator<Long>{
    @Override
    public Long mutate(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception ex){
            return 0L;
        }
    }
}
