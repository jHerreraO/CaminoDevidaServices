package com.template.securityTemplate.rulesException;


import com.template.securityTemplate.utils.LocaleUtil;
import lombok.Getter;

import java.util.Arrays;

@Getter
public class EnumInvalidArgumentException extends Exception {

    public EnumInvalidArgumentException(String parameter, Object argument, Class<?> enumClass) {
        super(String.format(LocaleUtil.defaultLocale, "Invalid argument %s in parameter %s. Valid values: %s",
                argument, parameter, Arrays.toString(enumClass.getEnumConstants())));
    }
}
