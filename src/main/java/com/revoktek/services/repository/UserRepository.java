package com.revoktek.services.repository;


import com.revoktek.services.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,Long>, JpaSpecificationExecutor<User> {

    User findByUsername(String username);
    User findFirstByUsernameOrderByDateRegisterDesc(String username);
    boolean existsByUsername(String username);
}
