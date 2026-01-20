package com.revoktek.services.model.dto.memberGroups;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class GroupUserDTO {
    private Integer age;
    private String names;
    private String phone;
    private String paternalSurname;
    private String maternalSurname;
    private String residencyCity;
}

