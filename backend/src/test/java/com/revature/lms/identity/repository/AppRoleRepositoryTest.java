package com.revature.lms.identity.repository;

import com.revature.lms.identity.domain.entity.AppRole;
import com.revature.lms.identity.domain.enums.RoleName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
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
class AppRoleRepositoryTest {
        
        @Autowired
        private AppRoleRepository appRoleRepository;

        @Test
        void shouldSaveAndRetrieveRole() {
                AppRole savedRole = appRoleRepository.saveAndFlush(
                        new AppRole(RoleName.ADMIN)
                );

                Optional<AppRole> result =
                        appRoleRepository.findById(savedRole.getRoleId());

                assertTrue(result.isPresent());
                assertEquals(RoleName.ADMIN, result.get().getName());
        }

        @Test
        void shouldGenerateRoleIdWhenSaved() {
                AppRole savedRole = appRoleRepository.saveAndFlush(
                        new AppRole(RoleName.TRAINER)
                );

                assertNotNull(savedRole.getRoleId());
                assertTrue(savedRole.getRoleId() > 0);
        }

        @Test
        void shouldFindRoleByName() {
                appRoleRepository.saveAndFlush(
                        new AppRole(RoleName.LEARNER)
                );

                Optional<AppRole> result =
                        appRoleRepository.findByName(RoleName.LEARNER);
                
                assertTrue(result.isPresent());
                assertEquals(RoleName.LEARNER, result.get().getName());
        }

        @Test
        void shouldReturnEmptyWhenRoleDoesNotExist() {
                Optional<AppRole> result =
                        appRoleRepository.findByName(RoleName.ADMIN);
                
                assertTrue(result.isEmpty());
        }

        @Test
        void shouldCheckWhetherRoleExists() {
                appRoleRepository.saveAndFlush(
                        new AppRole(RoleName.TRAINER)
                );

                assertTrue(appRoleRepository.existsByName(RoleName.TRAINER));
                assertFalse(appRoleRepository.existsByName(RoleName.ADMIN));
        }
}

