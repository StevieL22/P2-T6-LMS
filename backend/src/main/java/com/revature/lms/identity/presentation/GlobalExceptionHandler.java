package com.revature.lms.identity.presentation;

import com.revature.lms.identity.exception.AuthenticationFailedException;
import com.revature.lms.identity.exception.DuplicateEmailException;
import com.revature.lms.identity.exception.ResourceNotFoundException;
import com.revature.lms.identity.exception.RoleAlreadyAssignedException;
import com.revature.lms.identity.exception.LastActiveAdminException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationFailure(
            AuthenticationFailedException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateEmail(
            DuplicateEmailException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(RoleAlreadyAssignedException.class)
    public ResponseEntity<ApiErrorResponse> handleRoleAlreadyAssigned(
            RoleAlreadyAssignedException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationFailure(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> validationErrors = new LinkedHashMap<>();

        for (FieldError fieldError :
                exception.getBindingResult().getFieldErrors()) {

            validationErrors.putIfAbsent(
                    fieldError.getField(),
                    fieldError.getDefaultMessage() == null
                            ? "Invalid value."
                            : fieldError.getDefaultMessage()
            );
        }

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Request validation failed.",
                request.getRequestURI(),
                validationErrors
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableRequest(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Request body is missing or contains invalid JSON.",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected server error occurred.",
                request.getRequestURI()
        );
    }

        @ExceptionHandler(LastActiveAdminException.class)
        public ResponseEntity<ApiErrorResponse> handleLastActiveAdmin(
                LastActiveAdminException exception,
                HttpServletRequest request
        ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI()
        );
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiErrorResponse> handleAccessDenied(
                AccessDeniedException exception,
                HttpServletRequest request
        ) {
        return buildResponse(
                HttpStatus.FORBIDDEN,
                "You do not have permission to perform this action.",
                request.getRequestURI()
        );
        }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            String path
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                Map.of()
        );

        return ResponseEntity.status(status).body(response);
    }

    public record ApiErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path,
            Map<String, String> validationErrors
    ) {
    }
}
