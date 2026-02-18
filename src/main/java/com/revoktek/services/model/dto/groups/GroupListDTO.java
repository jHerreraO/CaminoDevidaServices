package com.revoktek.services.model.dto.groups;

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
public class GroupListDTO {

    private Long idGroup;
    private String name;
    private String address;
    private String phone;
    private String dayOfWeek;
    private LocalTime hour;
    private List<String> instructors; // nombres o emails
}

