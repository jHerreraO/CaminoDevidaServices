package com.revoktek.services.model.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDTO {

    @NotBlank
    private String username;
    private String password;
    private String authority;

    public String getUsuario() {
        return username != null ? username.trim().toUpperCase() : "";
    }

}