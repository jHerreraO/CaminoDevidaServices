package com.revoktek.services.repository;

import com.revoktek.services.model.GroupMember;
import com.revoktek.services.model.enums.GroupRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    List<GroupMember> findByUserIdUserAndRole(Long userId, GroupRole role);

    Optional<GroupMember> findByGroupIdGroupAndUserIdUserAndRole(
            Long idGroup,
            Long idUser,
            GroupRole role
    );
}
