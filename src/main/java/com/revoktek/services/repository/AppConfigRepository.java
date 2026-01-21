package com.revoktek.services.repository;

import com.revoktek.services.model.AppConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppConfigRepository extends JpaRepository<AppConfig,Long> {
    Boolean existsByConfigKey(String id);
}
