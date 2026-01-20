package com.revoktek.services.repository;

import com.revoktek.services.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group,Long> {

    boolean existsByNameAndDayOfWeek(String name, DayOfWeek dayOfWeek);
    List<Group> findByNameContainingIgnoreCase(String name);

}
