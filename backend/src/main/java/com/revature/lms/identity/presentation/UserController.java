package com.revature.lms.identity.presentation;

import com.revature.lms.identity.application.dto.UpdateUserProfileRequest;
import com.revature.lms.identity.application.dto.UserProfileResponse;
import com.revature.lms.identity.application.service.UserService;
import com.revature.lms.identity.security.LmsUserPrincipal;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserProfileResponse getMyProfile(
            @AuthenticationPrincipal LmsUserPrincipal principal
    ) {
        return userService.getUserProfile(principal.userId());
    }

    @PutMapping("/me")
    public UserProfileResponse updateMyProfile(
            @AuthenticationPrincipal LmsUserPrincipal principal,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        return userService.updateUserProfile(
                principal.userId(),
                request
        );
    }
}
