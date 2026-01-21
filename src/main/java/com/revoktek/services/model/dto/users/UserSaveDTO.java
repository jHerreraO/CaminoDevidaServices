package com.revoktek.services.model.dto.users;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSaveDTO {
    private String username;
    private String password;
    private Integer age;
    private String names;
    private String paternalSurname;
    private String maternalSurname;
    private String residenceCity;
    private String dependents;
    private String phone;
    private String role;
    private Integer numberDependents;
}
