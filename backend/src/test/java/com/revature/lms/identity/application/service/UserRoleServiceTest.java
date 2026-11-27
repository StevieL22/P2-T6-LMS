package com.revature.lms.identity.application.service;

import com.revature.lms.identity.domain.entity.AppRole;
import com.revature.lms.identity.domain.entity.User;
import com.revature.lms.identity.domain.enums.RoleName;
import com.revature.lms.identity.exception.ResourceNotFoundException;
import com.revature.lms.identity.exception.RoleAlreadyAssignedException;
import com.revature.lms.identity.exception.LastActiveAdminException;
import com.revature.lms.identity.repository.AppRoleRepository;
import com.revature.lms.identity.repository.UserRepository;
import com.revature.lms.identity.repository.UserRoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:lms-user-role-service-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS monolith",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=monolith",
        "spring.flyway.enabled=false",
        "spring.sql.init.mode=never"
})
@Import(UserRoleService.class)
class UserRoleServiceTest {

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppRoleRepository appRoleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    private User createUser() {
        return userRepository.saveAndFlush(
                new User(
                        "role@example.com",
                        "hashed-password",
                        "John",
                        "Doe",
                        null
                )
        );
    }

    private AppRole createRole(RoleName roleName) {
        return appRoleRepository.saveAndFlush(
                new AppRole(roleName)
        );
    }

    @Test
    void shouldAssignRoleToUser() {
        User user = createUser();
        createRole(RoleName.LEARNER);

        var result = userRoleService.assignRole(
                user.getUserId(),
                RoleName.LEARNER
        );

        assertNotNull(result.getUserRoleId());
        assertEquals(user.getUserId(), result.getUser().getUserId());
        assertEquals(RoleName.LEARNER, result.getRole().getName());
    }

    @Test
    void shouldRetrieveAssignedRole() {
        User user = createUser();
        createRole(RoleName.TRAINER);

        userRoleService.assignRole(
                user.getUserId(),
                RoleName.TRAINER
        );

        assertEquals(
                RoleName.TRAINER,
                userRoleService.getRoleForUser(user.getUserId())
        );
    }

    @Test
    void shouldPreventAssigningMoreThanOneRoleToUser() {
        User user = createUser();
        createRole(RoleName.LEARNER);

        userRoleService.assignRole(
                user.getUserId(),
                RoleName.LEARNER
        );

        assertThrows(
                RoleAlreadyAssignedException.class,
                () -> userRoleService.assignRole(
                        user.getUserId(),
                        RoleName.LEARNER
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenAssigningRoleToMissingUser() {
        createRole(RoleName.LEARNER);

        assertThrows(
                ResourceNotFoundException.class,
                () -> userRoleService.assignRole(
                        99999L,
                        RoleName.LEARNER
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenAssigningMissingRole() {
        User user = createUser();

        assertThrows(
                ResourceNotFoundException.class,
                () -> userRoleService.assignRole(
                        user.getUserId(),
                        RoleName.ADMIN
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenRetrievingRoleForUnassignedUser() {
        User user = createUser();

        assertThrows(
                ResourceNotFoundException.class,
                () -> userRoleService.getRoleForUser(user.getUserId())
        );
    }

        @Test
        void shouldChangeExistingUserRole() {
        User user = createUser();

        createRole(RoleName.LEARNER);
        createRole(RoleName.TRAINER);

        userRoleService.assignRole(user.getUserId(), RoleName.LEARNER);

        var updatedRole = userRoleService.changeRole(
                user.getUserId(),
                RoleName.TRAINER
        );

        assertEquals(RoleName.TRAINER, updatedRole.getRole().getName());
        assertEquals(
                RoleName.TRAINER,
                userRoleService.getRoleForUser(user.getUserId())
        );

        assertEquals(1, userRoleRepository.count());
        }

        @Test
        void shouldPreventDemotingTheLastActiveAdmin() {
        User user = createUser();

        createRole(RoleName.ADMIN);
        createRole(RoleName.LEARNER);

        userRoleService.assignRole(user.getUserId(), RoleName.ADMIN);

        assertThrows(
                LastActiveAdminException.class,
                () -> userRoleService.changeRole(
                        user.getUserId(),
                        RoleName.LEARNER
                )
        );

        assertEquals(
                RoleName.ADMIN,
                userRoleService.getRoleForUser(user.getUserId())
        );
        }

        @Test
        void shouldAllowDemotingAdminWhenAnotherActiveAdminExists() {
        User firstAdmin = userRepository.saveAndFlush(
                new User("first-admin@example.com", "hash", "First", "Admin", null)
        );

        User secondAdmin = userRepository.saveAndFlush(
                new User("second-admin@example.com", "hash", "Second", "Admin", null)
        );

        createRole(RoleName.ADMIN);
        createRole(RoleName.TRAINER);

        userRoleService.assignRole(firstAdmin.getUserId(), RoleName.ADMIN);
        userRoleService.assignRole(secondAdmin.getUserId(), RoleName.ADMIN);

        var updatedRole = userRoleService.changeRole(
                firstAdmin.getUserId(),
                RoleName.TRAINER
        );

        assertEquals(RoleName.TRAINER, updatedRole.getRole().getName());
        assertEquals(
                RoleName.ADMIN,
                userRoleService.getRoleForUser(secondAdmin.getUserId())
        );
        }

        @Test
        void shouldThrowExceptionWhenChangingRoleForMissingUser() {
        assertThrows(
                ResourceNotFoundException.class,
                () -> userRoleService.changeRole(
                        99999L,
                        RoleName.TRAINER
                )
        );
        }

        @Test
        void shouldThrowExceptionWhenChangingRoleForUnassignedUser() {
        User user = createUser();
        createRole(RoleName.TRAINER);

        assertThrows(
                ResourceNotFoundException.class,
                () -> userRoleService.changeRole(
                        user.getUserId(),
                        RoleName.TRAINER
                )
        );
        }

        @Test
        void shouldThrowExceptionWhenChangingToMissingRole() {
        User user = createUser();
        createRole(RoleName.LEARNER);

        userRoleService.assignRole(
                user.getUserId(),
                RoleName.LEARNER
        );

        assertThrows(
                ResourceNotFoundException.class,
                () -> userRoleService.changeRole(
                        user.getUserId(),
                        RoleName.TRAINER
                )
        );

        assertEquals(
                RoleName.LEARNER,
                userRoleService.getRoleForUser(user.getUserId())
        );
        }

        @Test
        void shouldAllowDemotingInactiveAdminWhenLastActiveAdmin() {
        User user = createUser();
        user.setActive(false);
        userRepository.saveAndFlush(user);

        createRole(RoleName.ADMIN);
        createRole(RoleName.LEARNER);

        userRoleService.assignRole(
                user.getUserId(),
                RoleName.ADMIN
        );

        var updatedRole = userRoleService.changeRole(
                user.getUserId(),
                RoleName.LEARNER
        );

        assertEquals(
                RoleName.LEARNER,
                updatedRole.getRole().getName()
        );
        }
}
