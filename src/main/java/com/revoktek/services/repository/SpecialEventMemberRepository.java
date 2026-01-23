package com.revoktek.services.repository;

import com.revoktek.services.model.SpecialEventMember;
import com.revoktek.services.model.WorshipMember;
import com.revoktek.services.model.enums.GroupRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpecialEventMemberRepository extends JpaRepository<SpecialEventMember,Long> {

    List<SpecialEventMember> findByUserIdUserAndRole(Long userId, GroupRole role);

    Optional<SpecialEventMember> findBySpecialEventIdSpecialEventAndUserIdUserAndRole(
            Long idWorship,
            Long idUser,
            GroupRole role
    );
}
