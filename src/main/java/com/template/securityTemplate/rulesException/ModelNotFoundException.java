package com.template.securityTemplate.rulesException;

import com.template.securityTemplate.utils.LocaleUtil;

public class ModelNotFoundException extends Exception {

    public ModelNotFoundException(Class<?> model, Object id) {
        super(String.format(LocaleUtil.defaultLocale, "'%s' not found.", model.getSimpleName()));
    }
}
