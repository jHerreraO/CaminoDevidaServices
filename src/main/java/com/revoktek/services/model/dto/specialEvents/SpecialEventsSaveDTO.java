package com.revoktek.services.model.dto.specialEvents;

import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpecialEventsSaveDTO {
    private String name;
    private String address;
    private String phone;
    private String dayOfWeek;
    private LocalTime hour;
    private Integer numberOfSlots;
}
