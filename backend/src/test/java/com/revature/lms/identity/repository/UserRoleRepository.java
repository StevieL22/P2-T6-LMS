package com.revature.lms.identity.repository;

import com.revature.lms.identity.domain.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    
    Optional<UserRole> findByUser_UserId(Long userId);

    boolean existsByUser_UserId(Long userId);

    Optional<UserRole> findByUserRoleIdAndUser_UserId(
            Long userRoleId,
            Long userId
    );
}
