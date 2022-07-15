package com.bedivierre.eloquent;

import com.bedivierre.eloquent.annotations.NotSqlField;
import com.bedivierre.eloquent.annotations.SqlField;
import com.bedivierre.eloquent.annotations.SqlFieldType;
import com.bedivierre.eloquent.model.DBModel;
import com.bedivierre.eloquent.mutators.*;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;
import java.sql.SQLException;

/*********************************
 ** Code by Bedivierre
 ** 15.07.2022 11:30
 **********************************/
public class DBResolver {


    public <T extends DBModel> ResultSet<T>
    sqlToModels(java.sql.ResultSet sqlResp, Class<T> model)
            throws SQLException, InstantiationException, IllegalAccessException {
        ResultSet<T> list = new ResultSet<>();
        if(model == null)
            return list;
        while(sqlResp.next()){
            T instance = createModel(sqlResp, model);
            list.add(instance);
        }
        return list;
    }
    private <T extends DBModel> T createModel(java.sql.ResultSet sqlResp, Class<T> model)
            throws InstantiationException, IllegalAccessException, SQLException {
        T instance = model.newInstance();
        Field[] fields = model.getDeclaredFields();
        for (Field field : fields) {
            processField(sqlResp, field, instance);
        }
        return instance;
    }
    private <T extends DBModel> boolean processField(java.sql.ResultSet sqlResp, Field field, T instance)
            throws InstantiationException, IllegalAccessException, SQLException {
        field.setAccessible(true);
        if(field.isAnnotationPresent(NotSqlField.class))
            return false;
        SqlField a = null;
        if(field.isAnnotationPresent(SqlField.class)) {
            a = field.getAnnotation(SqlField.class);
        }

        String column = field.getName();
        if(a != null && !StringUtils.isEmpty(a.column()))
            column = a.column();
        SqlFieldType type = a == null ? SqlFieldType.NONE : a.type();

        String fieldValue = sqlResp.getString(column);
        TypeMutator mutator = getFieldMutator(field, a);
        if(mutator == null){
            return false;
        }
        if(type == SqlFieldType.ARRAY && a.arrayElementType().length > 0)
            field.set(instance, mutator.mutateArray(fieldValue, a.arrayElementType()[0]));
        else if(type == SqlFieldType.MAP && a.arrayElementType().length > 0)
            field.set(instance, mutator.mutateMap(fieldValue, a.arrayElementType()[0]));
        else
            field.set(instance, mutator.mutate(fieldValue));
        return true;
    }


    private TypeMutator getFieldMutator(Field field, SqlField a){
        if(a == null || a.type() == SqlFieldType.NONE){
            return TypeMutator.getTypeMutator(field.getType());
        }
        return TypeMutator.getMutator(a.type().value);
    }
    private TypeMutator getFieldMutator(Field field){

        return getFieldMutator(field, null);
    }

}
