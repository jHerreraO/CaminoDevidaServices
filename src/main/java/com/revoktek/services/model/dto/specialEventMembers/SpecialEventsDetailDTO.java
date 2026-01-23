package com.revoktek.services.model.dto.specialEventMembers;

import com.revoktek.services.model.dto.memberWorkships.WorshipUserDTO;
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
public class SpecialEventsDetailDTO {
    private Long idSpecialEvent;
    private String name;
    private String address;
    private String phone;
    private LocalTime hour;
    private List<SpecialEventsUserDTO> members;
    private Integer numberOfSlots;
    private Integer slotsRemaining;
}
