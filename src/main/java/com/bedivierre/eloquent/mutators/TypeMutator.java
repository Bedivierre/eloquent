package com.bedivierre.eloquent.mutators;

import com.bedivierre.eloquent.annotations.SqlFieldType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*********************************
 ** Code by Bedivierre
 ** 15.07.2022 11:58
 **********************************/
public abstract class TypeMutator<T> {

    private static Map<String, TypeMutator> mutators = new HashMap<>();
    public static Map<String, TypeMutator> getMutators() { return mutators;}
    private static Map<Class, TypeMutator> typeMutators = new HashMap<>();
    public static Map<Class, TypeMutator> getTypeMutators() {
        return typeMutators;
    }

    private Class<T> modelFieldType;
    protected Class<T> getModelFieldType(){return modelFieldType;}

    public T mutate(SqlFieldType type, String value) { return null;}
    public T mutate(String value) {return null;}
    public List<T> mutateArray(String value, Class<T> model) {return null;}
    public Map<String, T> mutateMap(String value, Class<T> model) {return null;}


    public String toSql (T value){
        return value.toString();
    }
    public String arrayToSql (List<T> value){
        return value.toString();
    }
    public String mapToSql (Map<String, T> value){
        return value.toString();
    }
    public String toSql (T value, SqlFieldType type){
        switch (type){
            case DATE:
                return toSqlDate(value);
            case TIME:
                return toSqlTime(value);
            case DATETIME:
                return toSqlDateTime(value);
            case TIMESTAMP:
                return toSqlTimestamp(value);

            case STRING:
                return toSqlString(value);
            case INT:
                return toSqlInt(value);
            case LONG:
                return toSqlLong(value);
            case FLOAT:
                return toSqlFloat(value);
            case DECIMAL:
                return toSqlDecimal(value);
            default:
                return value.toString();
        }
    }
    public String toSqlInt (T value){return value.toString();}
    public String toSqlLong (T value){return value.toString();}
    public String toSqlFloat (T value){return value.toString();}
    public String toSqlDecimal (T value){return value.toString();}
    public String toSqlString (T value){return value.toString();}

    public String toSqlDate (T value){return value.toString();}
    public String toSqlTime (T value){return value.toString();}
    public String toSqlDateTime (T value){return value.toString();}
    public String toSqlTimestamp (T value){return value.toString();}





    public static void registerMutator(String name, TypeMutator mutator){
        getMutators().put(name, mutator);
    }
    public static TypeMutator getMutator(String name){
        return getMutators().getOrDefault(name, null);
    }

    public static void registerDefaultTypeMutator(Class type, TypeMutator mutator){
        getTypeMutators().put(type, mutator);
    }
    public static TypeMutator getTypeMutator(Class type){
        return getTypeMutators().getOrDefault(type, null);
    }
    static{
        registerMutator(SqlFieldType.INT.value, new IntMutator());
        registerMutator(SqlFieldType.BOOLEAN.value, new BooleanMutator());
        registerMutator(SqlFieldType.LONG.value, new LongMutator());
        registerMutator(SqlFieldType.STRING.value, new StringMutator());
        registerMutator(SqlFieldType.FLOAT.value, new FloatMutator());
        registerMutator(SqlFieldType.DOUBLE.value, new DoubleMutator());
        registerMutator(SqlFieldType.ARRAY.value, new ArrayMutator());



        registerDefaultTypeMutator(int.class, new IntMutator());
        registerDefaultTypeMutator(Integer.class, new IntMutator());
        registerDefaultTypeMutator(long.class, new LongMutator());
        registerDefaultTypeMutator(Long.class, new LongMutator());
        registerDefaultTypeMutator(float.class, new FloatMutator());
        registerDefaultTypeMutator(Float.class, new FloatMutator());
        registerDefaultTypeMutator(double.class, new DoubleMutator());
        registerDefaultTypeMutator(Double.class, new DoubleMutator());
        registerDefaultTypeMutator(boolean.class, new BooleanMutator());
        registerDefaultTypeMutator(Boolean.class, new BooleanMutator());
        registerDefaultTypeMutator(String.class, new StringMutator());
    }
}
