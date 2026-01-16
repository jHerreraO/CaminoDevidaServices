package com.template.securityTemplate.mapper;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Model {
    Class<?> typeList() default List.class;

    boolean isList() default false;

    boolean hasInsideModel() default false;
}