package com.template.securityTemplate.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Message {
    @JsonProperty("success")
    private Boolean success;
    @JsonProperty("message")
    private String message;
    @JsonProperty("data")
    private Object data;

    public Message(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}

