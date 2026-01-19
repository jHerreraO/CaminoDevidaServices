package com.revoktek.services.model.dto.groups;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class GroupSaveDTO {
    private String name;
    private String address;
    private String phone;
    private String dayOfWeek;
    private LocalTime hour;
    private String nameCategory;
}
