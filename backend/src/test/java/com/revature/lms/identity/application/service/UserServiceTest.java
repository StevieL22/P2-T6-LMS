package com.revature.lms.identity.application.service;

import com.revature.lms.identity.application.dto.RegisterUserRequest;
import com.revature.lms.identity.application.dto.UpdateUserProfileRequest;
import com.revature.lms.identity.application.dto.UserProfileResponse;
import com.revature.lms.identity.domain.entity.AppRole;
import com.revature.lms.identity.domain.enums.RoleName;
import com.revature.lms.identity.exception.DuplicateEmailException;
import com.revature.lms.identity.exception.ResourceNotFoundException;
import com.revature.lms.identity.repository.AppRoleRepository;
import com.revature.lms.identity.repository.UserRepository;
import com.revature.lms.identity.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
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
        "spring.datasource.url=jdbc:h2:mem:lms-user-service-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS monolith",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=monolith",
        "spring.flyway.enabled=false",
        "spring.sql.init.mode=never"
})
@Import({
        UserService.class,
        UserRoleService.class,
        PasswordService.class,
        com.revature.lms.identity.config.PasswordConfiguration.class
})
class UserServiceTest {
    
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppRoleRepository appRoleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @BeforeEach
    void setup() {
        appRoleRepository.saveAndFlush(
            new AppRole(RoleName.LEARNER)
        );
    }

    private RegisterUserRequest validRegistrationRequest(String email) {
        return new RegisterUserRequest(
                email,
                "StrongPassword123!",
                "John",
                "Doe",
                "1234567890"
        );
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        UserProfileResponse response = userService.registerUser(
                validRegistrationRequest("john@example.com")
        );

        assertNotNull(response.userId());
        assertEquals("john@example.com", response.email());
        assertEquals("John", response.firstName());
        assertEquals("Doe", response.lastName());
        assertEquals(RoleName.LEARNER, response.role());
        assertTrue(response.active());

        assertTrue(userRepository.existsById(response.userId()));
        assertTrue(userRoleRepository.existsByUser_UserId(response.userId()));
    }

    @Test
    void shouldNormalizeEmailDuringRegistration() {
        UserProfileResponse response = userService.registerUser(
                validRegistrationRequest("  JOHN@EXAMPLE.COM  ")
        );

        assertEquals("john@example.com", response.email());
    }

    @Test
    void shouldHashPasswordBeforeSavingUser() {
        userService.registerUser(
                validRegistrationRequest("password@example.com")
        );

        var savedUser = userRepository
                .findByNormalizedEmail("password@example.com")
                .orElseThrow();

        assertNotEquals("StrongPassword123!", savedUser.getPasswordHash());
        assertTrue(
                new PasswordService(
                        new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(4)
                ).verifyPassword(
                        "StrongPassword123!",
                        savedUser.getPasswordHash()
                )
        );
    }

    @Test
    void shouldAssignLearnerRoleRegardlessOfRegistrationData() {
        UserProfileResponse response = userService.registerUser(
                validRegistrationRequest("learner@example.com")
        );

        assertEquals(RoleName.LEARNER, response.role());
    }

    @Test
    void shouldRejectDuplicateEmailRegardlessOfCase() {
        userService.registerUser(
                validRegistrationRequest("john@example.com")
        );

        assertThrows(
                DuplicateEmailException.class,
                () -> userService.registerUser(
                        validRegistrationRequest("JOHN@EXAMPLE.COM")
                )
        );
    }

    @Test
    void shouldRejectDuplicateEmailWithWhitespace() {
        userService.registerUser(
                validRegistrationRequest("john@example.com")
        );

        assertThrows(
                DuplicateEmailException.class,
                () -> userService.registerUser(
                        validRegistrationRequest("  john@example.com  ")
                )
        );
    }

    @Test
    void shouldRetrieveUserProfile() {
        UserProfileResponse registered = userService.registerUser(
                validRegistrationRequest("profile@example.com")
        );

        UserProfileResponse response =
                userService.getUserProfile(registered.userId());

        assertEquals(registered.userId(), response.userId());
        assertEquals("profile@example.com", response.email());
        assertEquals(RoleName.LEARNER, response.role());
    }

    @Test
    void shouldUpdateUserProfile() {
        UserProfileResponse registered = userService.registerUser(
                validRegistrationRequest("update@example.com")
        );

        UpdateUserProfileRequest updateRequest =
                new UpdateUserProfileRequest(
                        "Jane",
                        "Smith",
                        "9876543210"
                );

        UserProfileResponse response = userService.updateUserProfile(
                registered.userId(),
                updateRequest
        );

        assertEquals("Jane", response.firstName());
        assertEquals("Smith", response.lastName());
        assertEquals("9876543210", response.phone());

        // Normal profile updates must not change email or role.
        assertEquals("update@example.com", response.email());
        assertEquals(RoleName.LEARNER, response.role());
    }

    @Test
    void shouldConvertBlankPhoneToNullDuringProfileUpdate() {
        UserProfileResponse registered = userService.registerUser(
                validRegistrationRequest("phone@example.com")
        );

        UserProfileResponse response = userService.updateUserProfile(
                registered.userId(),
                new UpdateUserProfileRequest("John", "Doe", " ")
        );

        assertNull(response.phone());
    }

    @Test
    void shouldThrowExceptionWhenRetrievingMissingUser() {
        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserProfile(99999L)
        );
    }

    @Test
    void shouldThrowExceptionWhenUpdatingMissingUser() {
        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.updateUserProfile(
                        99999L,
                        new UpdateUserProfileRequest(
                                "Jane",
                                "Smith",
                                null
                        )
                )
        );
    }
}
