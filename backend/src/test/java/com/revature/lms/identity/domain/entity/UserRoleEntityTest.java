package com.revature.lms.identity.domain.entity;

import com.revature.lms.identity.domain.enums.RoleName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserRoleEntityTest {

    private User user;
    private AppRole role;

    @BeforeEach
    void setUp() {
        user = new User(
                "test@example.com",
                "hashed-password",
                "John",
                "Doe",
                "1234567890"
        );

        role = new AppRole(RoleName.LEARNER);
    }

    @Test
    void constructorShouldInitializeUserAndRole() {
        UserRole userRole = new UserRole(user, role);

        assertNull(userRole.getUserRoleId());
        assertSame(user, userRole.getUser());
        assertSame(role, userRole.getRole());
    }

    @Test
    void shouldUpdateAssignedUser() {
        UserRole userRole = new UserRole(user, role);

        User replacementUser = new User(
                "another@example.com",
                "another-hash",
                "Jane",
                "Smith",
                null
        );

        userRole.setUser(replacementUser);

        assertSame(replacementUser, userRole.getUser());
    }

    @Test
    void shouldUpdateAssignedRole() {
        UserRole userRole = new UserRole(user, role);
        AppRole replacementRole = new AppRole(RoleName.ADMIN);

        userRole.setRole(replacementRole);

        assertSame(replacementRole, userRole.getRole());
        assertEquals(RoleName.ADMIN, userRole.getRole().getName());
    }
}
