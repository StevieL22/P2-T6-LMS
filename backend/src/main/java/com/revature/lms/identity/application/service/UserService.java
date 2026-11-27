package com.revature.lms.identity.application.service;

import com.revature.lms.identity.application.dto.RegisterUserRequest;
import com.revature.lms.identity.application.dto.UpdateUserProfileRequest;
import com.revature.lms.identity.application.dto.UserProfileResponse;
import com.revature.lms.identity.domain.entity.User;
import com.revature.lms.identity.domain.enums.RoleName;
import com.revature.lms.identity.exception.DuplicateEmailException;
import com.revature.lms.identity.exception.ResourceNotFoundException;
import com.revature.lms.identity.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final UserRoleService userRoleService;
    private final PasswordService passwordService;

    public UserService(
            UserRepository userRepository,
            UserRoleService userRoleService,
            PasswordService passwordService
    ) {
        this.userRepository = userRepository;
        this.userRoleService = userRoleService;
        this.passwordService = passwordService;
    }

    @Transactional
    public UserProfileResponse registerUser(RegisterUserRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        if(userRepository.existsByNormalizedEmail(normalizedEmail)) {
            throw new DuplicateEmailException(
                "An account already exists with this email address."
            );
        }

        String passwordHash = passwordService.hashPassword(
                request.password()
        );

        User user = new User(
                normalizedEmail,
                passwordHash,
                request.firstName().trim(),
                request.lastName().trim(),
                normalizePhone(request.phone())
        );

        User savedUser = userRepository.save(user);

        // Public registration always assigns LEARNER
        userRoleService.assignRole(
                savedUser.getUserId(),
                RoleName.LEARNER
        );

        return buildProfileResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                            "User not found with ID: " + userId
                        )
                );
        
        return buildProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateUserProfile(
            Long userId,
            UpdateUserProfileRequest request
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                            "User not found with ID: " + userId
                        )
                );
        
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setPhone(normalizePhone(request.phone()));

        User updatedUser = userRepository.save(user);

        return buildProfileResponse(updatedUser);
    }

    @Transactional
    public UserProfileResponse changeUserRole(
            Long userId,
            RoleName newRole
    ) {
        userRoleService.changeRole(userId, newRole);

        return getUserProfile(userId);
    }


    private UserProfileResponse buildProfileResponse(User user) {
        
        RoleName role = userRoleService.getRoleForUser(user.getUserId());

        return new UserProfileResponse(
                user.getUserId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getActive(),
                role,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }

        return phone.trim();
    }
}
