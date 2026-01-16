package com.template.securityTemplate.mapper;

public class UniqueFieldException extends Exception {
    private final String field;
    private final Object value;

    public UniqueFieldException(String field, Object value, String message) {
        super(field + message);
        this.field = field;
        this.value = value;
    }
}
