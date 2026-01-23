package com.revoktek.services.model.dto.specialEvents;

import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class PublicSpecialEventsDTO {
    private Long idSpecialEvent;
    private String name;
    private String address;
    private String phone;
    private String dayOfWeek;
    private LocalTime hour;
    private Integer numberOfSlots;
    private Integer slotsRemaining;
}
