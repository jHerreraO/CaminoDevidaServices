package com.revoktek.services.model.dto.memberGroups;
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
public class GroupDetailDTO {

    private Long idGroup;
    private String name;
    private String address;
    private String phone;
    private LocalTime hour;

    private List<GroupUserDTO> instructors;
    private List<GroupUserDTO> members;
}

