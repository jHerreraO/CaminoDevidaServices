package com.template.securityTemplate.repository;


import com.template.securityTemplate.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    User findByUsername(String username);
    User findFirstByUsernameOrderByDateRegisterDesc(String username);
    boolean existsByUsername(String username);
}
