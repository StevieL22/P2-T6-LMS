package com.revature.lms.identity.repository;

import com.revature.lms.identity.domain.entity.AppRole;
import com.revature.lms.identity.domain.entity.User;
import com.revature.lms.identity.domain.entity.UserRole;
import com.revature.lms.identity.domain.enums.RoleName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:lms-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS monolith",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=monolith",
        "spring.flyway.enabled=false",
        "spring.sql.init.mode=never"
})
class UserRoleRepositoryTest {

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppRoleRepository appRoleRepository;

    private User createAndSaveUser(String email) {
        User user = new User(
                email,
                "hashed-password",
                "John",
                "Doe",
                "1234567890"
        );

        return userRepository.saveAndFlush(user);
    }

    private AppRole createAndSaveRole(RoleName roleName) {
        return appRoleRepository.saveAndFlush(
                new AppRole(roleName)
        );
    }

    @Test
    void shouldSaveAndRetrieveUserRole() {
        User user = createAndSaveUser("john@example.com");
        AppRole role = createAndSaveRole(RoleName.LEARNER);

        UserRole savedUserRole = userRoleRepository.saveAndFlush(
                new UserRole(user, role)
        );

        Optional<UserRole> result =
                userRoleRepository.findById(savedUserRole.getUserRoleId());

        assertTrue(result.isPresent());
        assertEquals(user.getUserId(), result.get().getUser().getUserId());
        assertEquals(RoleName.LEARNER, result.get().getRole().getName());
    }

    @Test
    void shouldGenerateUserRoleIdWhenSaved() {
        User user = createAndSaveUser("generated@example.com");
        AppRole role = createAndSaveRole(RoleName.TRAINER);

        UserRole savedUserRole = userRoleRepository.saveAndFlush(
                new UserRole(user, role)
        );

        assertNotNull(savedUserRole.getUserRoleId());
        assertTrue(savedUserRole.getUserRoleId() > 0);
    }

    @Test
    void shouldFindUserRoleByUserId() {
        User user = createAndSaveUser("find@example.com");
        AppRole role = createAndSaveRole(RoleName.LEARNER);

        userRoleRepository.saveAndFlush(new UserRole(user, role));

        Optional<UserRole> result =
                userRoleRepository.findByUser_UserId(user.getUserId());

        assertTrue(result.isPresent());
        assertEquals(user.getUserId(), result.get().getUser().getUserId());
    }

    @Test
    void shouldReturnEmptyWhenUserHasNoAssignedRole() {
        User user = createAndSaveUser("norole@example.com");

        Optional<UserRole> result =
                userRoleRepository.findByUser_UserId(user.getUserId());

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldCheckWhetherUserHasAssignedRole() {
        User user = createAndSaveUser("assigned@example.com");
        AppRole role = createAndSaveRole(RoleName.ADMIN);

        userRoleRepository.saveAndFlush(new UserRole(user, role));

        assertTrue(userRoleRepository.existsByUser_UserId(user.getUserId()));
    }

    @Test
    void shouldReturnFalseWhenUserHasNoAssignedRole() {
        User user = createAndSaveUser("unassigned@example.com");

        assertFalse(userRoleRepository.existsByUser_UserId(user.getUserId()));
    }

    @Test
    void shouldPreventAssigningMultipleRolesToSameUser() {
        User user = createAndSaveUser("duplicate@example.com");

        AppRole learnerRole = createAndSaveRole(RoleName.LEARNER);
        AppRole adminRole = createAndSaveRole(RoleName.ADMIN);

        userRoleRepository.saveAndFlush(
                new UserRole(user, learnerRole)
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> userRoleRepository.saveAndFlush(
                        new UserRole(user, adminRole)
                )
        );
    }

    @Test
    void shouldAllowMultipleUsersToShareSameRole() {
        User firstUser = createAndSaveUser("first@example.com");
        User secondUser = createAndSaveUser("second@example.com");

        AppRole learnerRole = createAndSaveRole(RoleName.LEARNER);

        userRoleRepository.saveAndFlush(
                new UserRole(firstUser, learnerRole)
        );

        userRoleRepository.saveAndFlush(
                new UserRole(secondUser, learnerRole)
        );

        assertTrue(
                userRoleRepository.existsByUser_UserId(firstUser.getUserId())
        );

        assertTrue(
                userRoleRepository.existsByUser_UserId(secondUser.getUserId())
        );
    }
}
