package com.revoktek.services.model.dto.workships;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class WorshipListDTO {
    private Long idWorship;
    private String name;
    private String address;
    private String phone;
    private String dayOfWeek;
    private LocalTime hour;
}
