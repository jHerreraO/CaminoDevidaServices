package com.template.securityTemplate.rulesException;

import com.template.securityTemplate.utils.LocaleUtil;

public class DuplicateModelException extends Exception {
    public DuplicateModelException(Class<?> clazz, String value, String property) {
        super(String.format(LocaleUtil.defaultLocale,
                " A %s with %s equal to %s is already registered",
                clazz.getSimpleName(), property, value));
    }
}
