package com.revoktek.services.repository;

import com.revoktek.services.model.SpecialEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecialEventRepository extends JpaRepository<SpecialEvent,Long> {
}
