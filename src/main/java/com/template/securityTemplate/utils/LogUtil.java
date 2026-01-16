package com.template.securityTemplate.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@AllArgsConstructor
@Getter
@Setter
@Log4j2
public class LogUtil {
    private Exception exception;
    private String message;


    public void LogError(Exception exception, String message){
        String exceptionMessage = exception.getMessage();
        log.error(exceptionMessage);
    }

}
