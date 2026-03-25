package com.revoktek.services.model.dto.groups;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InstructorDTO {
    private Long idUser;
    private String fullName;
}