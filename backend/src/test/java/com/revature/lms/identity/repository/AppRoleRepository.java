package com.revature.lms.identity.repository;

import com.revature.lms.identity.domain.entity.AppRole;
import com.revature.lms.identity.domain.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface AppRoleRepository extends JpaRepository<AppRole, Long> {
    
    Optional<AppRole> findByName(RoleName name);

    boolean existsByName(RoleName name);
}
