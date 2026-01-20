package com.revoktek.services.repository;


import com.revoktek.services.model.User;
import com.revoktek.services.model.enums.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User,Long>, JpaSpecificationExecutor<User> {

    User findByUsername(String username);
    User findFirstByUsernameOrderByDateRegisterDesc(String username);
    boolean existsByUsername(String username);

    /**
     * Obtiene todos los usuarios que contienen una autoridad específica.
     *
     * @param authority Rol a buscar (ej. INSTRUCTOR).
     * @return Lista de usuarios con dicho rol.
     */
    List<User> findByAuthoritiesContaining(Authority authority);
}
