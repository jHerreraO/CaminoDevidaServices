package com.revoktek.services.mapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DTOUniqueField {
    String message() default " field value is already registered";
    // left implementation
    // Class<?>[] groups() default { };
}
