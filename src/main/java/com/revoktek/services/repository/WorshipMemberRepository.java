package com.revoktek.services.repository;

import com.revoktek.services.model.GroupMember;
import com.revoktek.services.model.WorshipMember;
import com.revoktek.services.model.enums.GroupRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorshipMemberRepository extends JpaRepository<WorshipMember,Long> {
    List<WorshipMember> findByUserIdUserAndRole(Long userId, GroupRole role);

    Optional<WorshipMember> findByWorshipIdWorshipAndUserIdUserAndRole(
            Long idWorship,
            Long idUser,
            GroupRole role
    );
}
