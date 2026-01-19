package com.revoktek.services.service;


import com.revoktek.services.model.Group;
import com.revoktek.services.model.dto.groups.GroupSaveDTO;
import com.revoktek.services.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Log4j2
public class GroupService {
    private final GroupRepository groupRepository;
    private final CategoryService categoryService;
    private final UtilService utilService;

    public void save(GroupSaveDTO groupSaveDTO){
        Group group = Group.builder()
                .address(groupSaveDTO.getAddress())
                .phone(groupSaveDTO.getPhone())
                .dayOfWeek(groupSaveDTO.getPhone())
                .hour(groupSaveDTO.getHour())
                .name(groupSaveDTO.getName())
                .build();

        group.setUserResponsible(utilService.userInSession());
        categoryService.save(groupSaveDTO.getNameCategory());
        groupRepository.save(group);
        log.info("Se ha registrado el siguiente grupo: " +groupSaveDTO.getName());

    }

}
