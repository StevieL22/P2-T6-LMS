package com.revature.lms.identity.presentation;

import com.revature.lms.identity.application.dto.AuthenticationResponse;
import com.revature.lms.identity.application.dto.LoginRequest;
import com.revature.lms.identity.application.dto.RegisterUserRequest;
import com.revature.lms.identity.application.dto.UserProfileResponse;
import com.revature.lms.identity.application.service.AuthenticationService;
import com.revature.lms.identity.application.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    
    private final UserService userService;
    private final AuthenticationService authenticationService;

    public AuthController(
        UserService userService,
        AuthenticationService authenticationService
    ) {
        this.userService = userService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserProfileResponse> register(
            @Valid @RequestBody RegisterUserRequest request
    ) {
        UserProfileResponse response = userService.registerUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthenticationResponse response =
                authenticationService.authenticate(request);

        return ResponseEntity.ok(response);
    }
    
    
}
