package com.revoktek.services.model.dto.specialEvents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class SpecialEventsListDTO {

    private Long idSpecialEvent;
    private String name;
    private String address;
    private String phone;
    private String dayOfWeek;
    private LocalTime hour;
    private Integer numberOfSlots;
    private Integer slotsRemaining;
}
