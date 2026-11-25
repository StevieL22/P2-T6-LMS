package com.revature.lms.identity.domain.entity;

import com.revature.lms.identity.domain.enums.RoleName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppRoleEntityTest {
    
    @Test
    void constructorShouldInitializeRoleName() {
        AppRole role = new AppRole(RoleName.ADMIN);

        assertNull(role.getRoleId());
        assertEquals(RoleName.ADMIN, role.getName());
    }

    @Test
    void shouldUpdateRoleName() {
        AppRole role = new AppRole(RoleName.LEARNER);

        role.setName(RoleName.TRAINER);

        assertEquals(RoleName.TRAINER, role.getName());
    }

    @Test
    void shouldSupportAllDefinedRoleNames() {
        assertEquals(3, RoleName.values().length);

        assertNotNull(RoleName.valueOf("ADMIN"));
        assertNotNull(RoleName.valueOf("TRAINER"));
        assertNotNull(RoleName.valueOf("LEARNER"));
    }
}
