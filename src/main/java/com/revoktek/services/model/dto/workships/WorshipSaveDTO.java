package com.revoktek.services.model.dto.workships;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class WorshipSaveDTO {
    private String name;
    private String address;
    private String phone;
    private String dayOfWeek;
    private LocalTime hour;
}
