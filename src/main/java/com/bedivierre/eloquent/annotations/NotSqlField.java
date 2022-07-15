package com.bedivierre.eloquent.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*********************************
 ** Code by Bedivierre
 ** 15.07.2022 11:24
 **********************************/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface NotSqlField {
}
