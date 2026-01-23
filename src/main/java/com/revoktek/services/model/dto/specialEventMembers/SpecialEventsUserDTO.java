package com.revoktek.services.model.dto.specialEventMembers;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class SpecialEventsUserDTO {
    private Integer age;
    private String names;
    private String phone;
    private String paternalSurname;
    private String maternalSurname;
    private String residencyCity;
}
