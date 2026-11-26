package com.revature.lms.identity.presentation;

import com.revature.lms.identity.exception.AuthenticationFailedException;
import com.revature.lms.identity.exception.DuplicateEmailException;
import com.revature.lms.identity.exception.ResourceNotFoundException;
import com.revature.lms.identity.exception.RoleAlreadyAssignedException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = createRequest("/api/v1/test");
    }

    @Test
    void shouldReturnUnauthorizedForAuthenticationFailure() {
        ResponseEntity<GlobalExceptionHandler.ApiErrorResponse> response =
                handler.handleAuthenticationFailure(
                        new AuthenticationFailedException("Invalid credentials."),
                        request
                );

        assertErrorResponse(
                response,
                HttpStatus.UNAUTHORIZED,
                "Invalid credentials."
        );
    }

    @Test
    void shouldReturnConflictForDuplicateEmail() {
        ResponseEntity<GlobalExceptionHandler.ApiErrorResponse> response =
                handler.handleDuplicateEmail(
                        new DuplicateEmailException("Email already exists."),
                        request
                );

        assertErrorResponse(
                response,
                HttpStatus.CONFLICT,
                "Email already exists."
        );
    }

    @Test
    void shouldReturnNotFoundForMissingResource() {
        ResponseEntity<GlobalExceptionHandler.ApiErrorResponse> response =
                handler.handleNotFound(
                        new ResourceNotFoundException("User not found."),
                        request
                );

        assertErrorResponse(
                response,
                HttpStatus.NOT_FOUND,
                "User not found."
        );
    }

    @Test
    void shouldReturnConflictWhenRoleIsAlreadyAssigned() {
        ResponseEntity<GlobalExceptionHandler.ApiErrorResponse> response =
                handler.handleRoleAlreadyAssigned(
                        new RoleAlreadyAssignedException("User already has a role."),
                        request
                );

        assertErrorResponse(
                response,
                HttpStatus.CONFLICT,
                "User already has a role."
        );
    }

    @Test
    void shouldReturnBadRequestWithValidationErrors() throws Exception {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "request");

        bindingResult.addError(
                new FieldError("request", "email", "Email is required.")
        );

        bindingResult.addError(
                new FieldError("request", "password", null)
        );

        MethodArgumentNotValidException exception =
                createValidationException(bindingResult);

        ResponseEntity<GlobalExceptionHandler.ApiErrorResponse> response =
                handler.handleValidationFailure(exception, request);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());

        GlobalExceptionHandler.ApiErrorResponse body = response.getBody();

        assertEquals("Bad Request", body.error());
        assertEquals("Request validation failed.", body.message());
        assertEquals("/api/v1/test", body.path());
        assertNotNull(body.timestamp());

        assertEquals(2, body.validationErrors().size());
        assertEquals(
                "Email is required.",
                body.validationErrors().get("email")
        );
        assertEquals(
                "Invalid value.",
                body.validationErrors().get("password")
        );
    }

    @Test
    void shouldKeepFirstValidationMessageForDuplicateFieldErrors() throws Exception {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "request");

        bindingResult.addError(
                new FieldError("request", "email", "Email is required.")
        );

        bindingResult.addError(
                new FieldError("request", "email", "Email format is invalid.")
        );

        MethodArgumentNotValidException exception =
                createValidationException(bindingResult);

        ResponseEntity<GlobalExceptionHandler.ApiErrorResponse> response =
                handler.handleValidationFailure(exception, request);

        assertNotNull(response.getBody());
        assertEquals(
                "Email is required.",
                response.getBody().validationErrors().get("email")
        );
        assertEquals(1, response.getBody().validationErrors().size());
    }

    @Test
    void shouldReturnBadRequestForUnreadableRequestBody() {
        HttpMessageNotReadableException exception =
                new HttpMessageNotReadableException(
                        "Malformed JSON.",
                        new MockHttpInputMessage(new byte[0])
                );

        ResponseEntity<GlobalExceptionHandler.ApiErrorResponse> response =
                handler.handleUnreadableRequest(exception, request);

        assertErrorResponse(
                response,
                HttpStatus.BAD_REQUEST,
                "Request body is missing or contains invalid JSON."
        );
    }

    @Test
    void shouldReturnInternalServerErrorForUnexpectedException() {
        ResponseEntity<GlobalExceptionHandler.ApiErrorResponse> response =
                handler.handleUnexpectedException(
                        new RuntimeException("Internal implementation details."),
                        request
                );

        assertErrorResponse(
                response,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected server error occurred."
        );

        assertNotEquals(
                "Internal implementation details.",
                response.getBody().message()
        );
    }

    private void assertErrorResponse(
            ResponseEntity<GlobalExceptionHandler.ApiErrorResponse> response,
            HttpStatus expectedStatus,
            String expectedMessage
    ) {
        assertNotNull(response);
        assertEquals(expectedStatus.value(), response.getStatusCode().value());

        GlobalExceptionHandler.ApiErrorResponse body = response.getBody();

        assertNotNull(body);
        assertNotNull(body.timestamp());
        assertInstanceOf(Instant.class, body.timestamp());

        assertEquals(expectedStatus.value(), body.status());
        assertEquals(expectedStatus.getReasonPhrase(), body.error());
        assertEquals(expectedMessage, body.message());
        assertEquals("/api/v1/test", body.path());
        assertEquals(Map.of(), body.validationErrors());
    }

    private MockHttpServletRequest createRequest(String path) {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI(path);
        return mockRequest;
    }

    private MethodArgumentNotValidException createValidationException(
            BeanPropertyBindingResult bindingResult
    ) throws Exception {
        Method method = GlobalExceptionHandlerTest.class
                .getDeclaredMethod("validationTarget", String.class);

        MethodParameter methodParameter = new MethodParameter(method, 0);

        return new MethodArgumentNotValidException(
                methodParameter,
                bindingResult
        );
    }

    @SuppressWarnings("unused")
    private void validationTarget(String value) {
        // Used only to provide a MethodParameter for validation tests.
    }
}

