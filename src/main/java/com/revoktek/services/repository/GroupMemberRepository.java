package com.revoktek.services.repository;

import com.revoktek.services.model.Group;
import com.revoktek.services.model.GroupMember;
import com.revoktek.services.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    boolean existsByGroupAndUser(Group group, User user);
}
