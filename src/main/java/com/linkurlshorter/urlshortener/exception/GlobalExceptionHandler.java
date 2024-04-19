package com.linkurlshorter.urlshortener.exception;

import com.linkurlshorter.urlshortener.auth.exception.EmailAlreadyTakenException;
import com.linkurlshorter.urlshortener.user.NoSuchEmailFoundException;
import com.linkurlshorter.urlshortener.user.NoUserFoundByEmailException;
import com.linkurlshorter.urlshortener.user.NoUserFoundByIdException;
import com.linkurlshorter.urlshortener.user.NullEmailException;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Global exception handler to catch and handle various types of errors throughout the application.
 *
 * @author Vlas Pototskyi
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Handles method argument validation errors and invalid request errors (400).
     * Returns a response with status 400 and the corresponding error message.
     *
     * @param exception method argument validation error
     * @return {@link ResponseEntity} object with the appropriate status and error message
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BadRequestException.class})
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed!",
                Objects.requireNonNull(exception.getFieldError()).getDefaultMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handles the null email error (400).
     * Returns a response with a 400 status and the corresponding error message.
     *
     * @param ex null email error
     * @return {@link ResponseEntity} object with the corresponding status and error message
     */
    @ExceptionHandler(NullEmailException.class)
    public ResponseEntity<Object> handleNullEmailException(NullEmailException ex) {
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.BAD_REQUEST,
                "Email provided is null, so request can not be processed!", ex.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handles AuthenticationException thrown during user authentication.
     *
     * @param ex the AuthenticationException thrown
     * @return {@link ResponseEntity} containing the error response for authentication failure
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex) {
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.UNAUTHORIZED,
                "Authentication failed!", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handles EmailAlreadyTakenException thrown during user registration.
     *
     * @param ex the EmailAlreadyTakenException thrown
     * @return {@link ResponseEntity} containing the error response for email already taken
     */
    @ExceptionHandler(EmailAlreadyTakenException.class)
    public ResponseEntity<Object> handleEmailAlreadyTakenException(EmailAlreadyTakenException ex) {
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.BAD_REQUEST,
                "Email already taken!", ex.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handles no resource (404) exceptions for different types of requests.
     * Returns a response with a 404 status and the corresponding error message.
     *
     * @param ex missing resource exception
     * @return {@link ResponseEntity} object with the corresponding status and error message
     */
    @ExceptionHandler({NoSuchEmailFoundException.class, NoUserFoundByEmailException.class, NoUserFoundByIdException.class})
    public ResponseEntity<Object> handleNotFoundExceptions(Exception ex) {
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.NOT_FOUND, "Email Not Found!",
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Creates an error response object.
     *
     * @param status           status of the error
     * @param message          error message
     * @param exceptionMessage exception message
     * @return an {@link ErrorResponse} object with the appropriate data
     */
    private ErrorResponse buildErrorResponse(HttpStatus status, String message, String exceptionMessage) {
        return new ErrorResponse(LocalDateTime.now(), status.value(), message, exceptionMessage);
    }
}

