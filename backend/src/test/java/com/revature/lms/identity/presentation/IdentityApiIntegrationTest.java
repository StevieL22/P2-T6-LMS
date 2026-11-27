package com.revature.lms.identity.presentation;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.revature.lms.identity.domain.enums.RoleName;
import com.revature.lms.identity.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:lms-api-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS monolith",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=monolith",
        "spring.flyway.enabled=false",
        "spring.sql.init.mode=never",
        "app.security.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "app.security.jwt.issuer=https://lms.revature.com",
        "app.security.jwt.expiration=PT15M",
        "app.security.login.max-failed-attempts=5",
        "app.security.login.lockout-duration=PT15M",
        "app.cors.allowed-origins=http://localhost:3000"
})
@AutoConfigureMockMvc
class IdentityApiIntegrationTest {

    private static final String AUTH_URL = "/api/v1/auth";
    private static final String USERS_URL = "/api/v1/users";

    private static final String PASSWORD = "SecurePassword123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private com.revature.lms.identity.repository.UserRepository userRepository;

    @Autowired
    private com.revature.lms.identity.repository.AppRoleRepository appRoleRepository;

    @Autowired
    private com.revature.lms.identity.repository.UserRoleRepository userRoleRepository;

    @BeforeEach
    void setUp() {
        userRoleRepository.deleteAll();
        userRepository.deleteAll();
        appRoleRepository.deleteAll();

        for (RoleName roleName : RoleName.values()) {
            appRoleRepository.save(
                    new com.revature.lms.identity.domain.entity.AppRole(roleName)
            );
        }
    }

    @Test
    void shouldRegisterUserWithoutAuthentication() throws Exception {
        mockMvc.perform(post(AUTH_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationJson(
                                "learner@example.com",
                                "Jordan",
                                "Smith"
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").isNumber())
                .andExpect(jsonPath("$.email")
                        .value("learner@example.com"))
                .andExpect(jsonPath("$.role").value("LEARNER"));
    }

    @Test
    void shouldLoginAndReturnJwt() throws Exception {
        registerUser("login@example.com");

        MvcResult result = mockMvc.perform(post(AUTH_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("login@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.role").value("LEARNER"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andReturn();

        JsonNode response = objectMapper.readTree(
                result.getResponse().getContentAsString()
        );

        String token = response.get("accessToken").asText();

        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void shouldRejectProfileRequestWithoutToken() throws Exception {
        mockMvc.perform(get(USERS_URL + "/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnProfileWithValidToken() throws Exception {
        registerUser("profile@example.com");

        String token = loginAndGetToken("profile@example.com");

        mockMvc.perform(get(USERS_URL + "/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email")
                        .value("profile@example.com"))
                .andExpect(jsonPath("$.role").value("LEARNER"));
    }

    @Test
    void shouldRejectInvalidJwt() throws Exception {
        mockMvc.perform(get(USERS_URL + "/me")
                        .header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectExpiredJwt() throws Exception {
        String token = createExpiredToken(
                1L,
                "expired@example.com",
                RoleName.LEARNER
        );

        mockMvc.perform(get(USERS_URL + "/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldUpdateAuthenticatedUsersProfile() throws Exception {
        registerUser("update@example.com");

        String token = loginAndGetToken("update@example.com");

        mockMvc.perform(put(USERS_URL + "/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "firstName": "Updated",
                                    "lastName": "User",
                                    "phone": "2015551234"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.phone").value("2015551234"));
    }

    @Test
    void shouldOnlyUpdateAuthenticatedUsersProfile() throws Exception {
        registerUser("first@example.com");
        registerUser("second@example.com");

        String token = loginAndGetToken("first@example.com");

        mockMvc.perform(put(USERS_URL + "/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "firstName": "Changed",
                                    "lastName": "Name",
                                    "phone": "2015559999"
                                }
                                """))
                .andExpect(status().isOk());

        var secondUser = userRepository
                .findByNormalizedEmail("second@example.com")
                .orElseThrow();

        assertEquals("Jordan", secondUser.getFirstName());
        assertEquals("Smith", secondUser.getLastName());

        var firstUser = userRepository
                .findByNormalizedEmail("first@example.com")
                .orElseThrow();

        assertEquals("Changed", firstUser.getFirstName());
        assertEquals("Jordan", secondUser.getFirstName());
    }

    @Test
    void shouldRejectInvalidRegistrationRequest() throws Exception {
        mockMvc.perform(post(AUTH_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "not-an-email",
                                  "password": "short",
                                  "firstName": "",
                                  "lastName": "Smith",
                                  "phone": "123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").exists());
    }

    @Test
    void shouldRejectInvalidProfileUpdate() throws Exception {
        registerUser("invalid-profile@example.com");

        String token = loginAndGetToken("invalid-profile@example.com");

        mockMvc.perform(put(USERS_URL + "/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "",
                                  "lastName": "",
                                  "phone": "12345678901234567890"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAcceptAllSupportedRoleClaims() throws Exception {
        registerUser("roles@example.com");

        var user = userRepository
                .findByNormalizedEmail("roles@example.com")
                .orElseThrow();

        for (RoleName role : RoleName.values()) {
            String token = jwtService.generateToken(
                    user.getUserId(),
                    user.getEmail(),
                    role
            );

            mockMvc.perform(get(USERS_URL + "/me")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        }
    }

        @Test
        void shouldAllowAdminToChangeUserRole() throws Exception {
        registerUser("trainer-candidate@example.com");

        var targetUser = userRepository
                .findByNormalizedEmail("trainer-candidate@example.com")
                .orElseThrow();

        String adminToken = jwtService.generateToken(
                999L,
                "admin@example.com",
                RoleName.ADMIN
        );

        mockMvc.perform(patch(USERS_URL + "/" + targetUser.getUserId() + "/role")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                        "role": "TRAINER"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(
                        targetUser.getUserId()
                ))
                .andExpect(jsonPath("$.role").value("TRAINER"));

        assertEquals(
                RoleName.TRAINER,
                userRoleRepository.findByUser_UserId(targetUser.getUserId())
                        .orElseThrow()
                        .getRole()
                        .getName()
        );
        }

        @Test
        void shouldRejectLearnerChangingUserRole() throws Exception {
        registerUser("learner-role-test@example.com");

        var targetUser = userRepository
                .findByNormalizedEmail("learner-role-test@example.com")
                .orElseThrow();

        String learnerToken = jwtService.generateToken(
                targetUser.getUserId(),
                targetUser.getEmail(),
                RoleName.LEARNER
        );

        mockMvc.perform(patch(USERS_URL + "/" + targetUser.getUserId() + "/role")
                        .header("Authorization", "Bearer " + learnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                        "role": "TRAINER"
                                }
                                """))
                .andExpect(status().isForbidden());

        assertEquals(
                RoleName.LEARNER,
                userRoleRepository.findByUser_UserId(targetUser.getUserId())
                        .orElseThrow()
                        .getRole()
                        .getName()
        );
        }

    private void registerUser(String email) throws Exception {
        mockMvc.perform(post(AUTH_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationJson(email, "Jordan", "Smith")))
                .andExpect(status().isCreated());
    }

    private String loginAndGetToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post(AUTH_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(
                result.getResponse().getContentAsString()
        ).get("accessToken").asText();
    }

    private String registrationJson(
            String email,
            String firstName,
            String lastName
    ) {
        return """
                {
                  "email": "%s",
                  "password": "%s",
                  "firstName": "%s",
                  "lastName": "%s",
                  "phone": "2015551234"
                }
                """.formatted(email, PASSWORD, firstName, lastName);
    }

    private String loginJson(String email) {
        return """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, PASSWORD);
    }

    private String createExpiredToken(
            Long userId,
            String email,
            RoleName role
    ) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("https://lms.revature.com")
                .issuedAt(now.minusSeconds(3600))
                .expiresAt(now.minusSeconds(60))
                .subject(userId.toString())
                .claim("email", email)
                .claim("role", role.name())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }
}
