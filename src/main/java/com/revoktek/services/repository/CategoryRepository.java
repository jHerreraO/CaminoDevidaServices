package com.revoktek.services.repository;

import com.revoktek.services.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category,Long> {
    boolean existByNameCategory(String name);
}
