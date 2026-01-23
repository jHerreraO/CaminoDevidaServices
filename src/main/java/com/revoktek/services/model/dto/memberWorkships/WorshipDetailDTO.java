package com.revoktek.services.model.dto.memberWorkships;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class WorshipDetailDTO {

    private Long idWorhship;
    private String name;
    private String address;
    private String phone;
    private LocalTime hour;
    private List<WorshipUserDTO> members;
}
