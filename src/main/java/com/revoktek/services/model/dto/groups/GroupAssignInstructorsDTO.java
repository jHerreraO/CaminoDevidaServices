package com.revoktek.services.model.dto.groups;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GroupAssignInstructorsDTO {
    private Long groupId;
    private List<Long> instructorIds;
}

