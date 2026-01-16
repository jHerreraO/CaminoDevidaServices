package com.template.securityTemplate.rulesException;

public class UniqueFieldException  extends Exception{
    public UniqueFieldException(Class<?> model, String field , String cause) {
        super(String.format("field '%s' already exists to parameter '%s' in model '%s'", field, cause, model.getSimpleName()));
    }
}
