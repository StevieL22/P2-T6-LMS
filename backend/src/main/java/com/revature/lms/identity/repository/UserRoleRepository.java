package com.revature.lms.identity.repository;

import com.revature.lms.identity.domain.entity.UserRole;
import com.revature.lms.identity.domain.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    @EntityGraph(attributePaths = "role")
    Optional<UserRole> findByUser_UserId(Long userId);

    boolean existsByUser_UserId(Long userId);

    Optional<UserRole> findByUserRoleIdAndUser_UserId(
            Long userRoleId,
            Long userId
    );

    @Query("""
            SELECT COUNT(ur)
            FROM UserRole ur
            WHERE ur.role.name = :roleName
              AND ur.user.active = TRUE
            """)
    long countActiveUsersByRole(@Param("roleName") RoleName roleName);
}
