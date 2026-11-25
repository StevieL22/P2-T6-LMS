package com.revature.lms.identity.application.service;

import com.revature.lms.identity.domain.entity.AppRole;
import com.revature.lms.identity.domain.entity.User;
import com.revature.lms.identity.domain.entity.UserRole;
import com.revature.lms.identity.domain.enums.RoleName;
import com.revature.lms.identity.exception.ResourceNotFoundException;
import com.revature.lms.identity.exception.RoleAlreadyAssignedException;
import com.revature.lms.identity.repository.AppRoleRepository;
import com.revature.lms.identity.repository.UserRepository;
import com.revature.lms.identity.repository.UserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final AppRoleRepository appRoleRepository;

    public UserRoleService(
            UserRoleRepository userRoleRepository,
            UserRepository userRepository,
            AppRoleRepository appRoleRepository
    ) {
        this.userRoleRepository = userRoleRepository;
        this.userRepository = userRepository;
        this.appRoleRepository = appRoleRepository;
    }

    @Transactional
    public UserRole assignRole(Long userId, RoleName roleName) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: " + userId
                        )
                );

        AppRole role = appRoleRepository.findByName(roleName)
                    .orElseThrow(() ->
                        new ResourceNotFoundException(
                            "ROle not found: " + roleName
                        )
                    );
        
        if (userRoleRepository.existsByUser_UserId(userId)) {
            throw new RoleAlreadyAssignedException(
                    "User already has an assigned role."
            );
        }

        UserRole userRole = new UserRole(user, role);

        return userRoleRepository.save(userRole);
    }

    @Transactional(readOnly = true)
    public RoleName getRoleForUser(Long userId) {

        UserRole userRole = userRoleRepository
                .findByUser_UserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No role assignment found for user ID: " + userId
                        )
                );
        
        return userRole.getRole().getName();
    }
}
