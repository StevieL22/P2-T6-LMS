package com.revature.lms.identity.repository;

import com.revature.lms.identity.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Finds a user by email without considering case or
     * leading/trailing whitespace.
     */
    @Query("""
            SELECT u
            FROM User u
            WHERE LOWER(TRIM(u.email)) = LOWER(TRIM(:email))
            """)
    Optional<User> findByNormalizedEmail(@Param("email") String email);

    boolean existsByEmailIgnoreCase(String email);
}
