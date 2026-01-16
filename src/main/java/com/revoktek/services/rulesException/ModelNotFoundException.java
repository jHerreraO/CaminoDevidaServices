package com.revoktek.services.rulesException;

import com.revoktek.services.utils.LocaleUtil;

public class ModelNotFoundException extends Exception {

    public ModelNotFoundException(Class<?> model, Object id) {
        super(String.format(LocaleUtil.defaultLocale, "'%s' not found.", model.getSimpleName()));
    }
}
