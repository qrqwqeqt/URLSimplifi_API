package com.linkurlshorter.urlshortener.exception;

import com.linkurlshorter.urlshortener.security.ForbiddenException;
import com.linkurlshorter.urlshortener.security.UnauthorizedException;
import com.linkurlshorter.urlshortener.user.NoSuchEmailFoundException;
import com.linkurlshorter.urlshortener.user.NoUserFoundByEmailException;
import com.linkurlshorter.urlshortener.user.NoUserFoundByIdException;
import com.linkurlshorter.urlshortener.user.NullEmailException;
import org.apache.coyote.BadRequestException;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

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
                exception.getMessage());
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
     * Handles authentication failure (401) errors.
     * Returns a response with a 401 status and the corresponding error message.
     *
     * @param ex failed authentication error
     * @return {@link ResponseEntity} object with the corresponding status and error message
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Object> handleUnauthorizedException(UnauthorizedException ex) {
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.UNAUTHORIZED,
                "Unauthorized!", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handles bad credentials (401) errors.
     * Returns a response with a 401 status and the corresponding error message.
     *
     * @param ex bad credentials error
     * @return {@link ResponseEntity} object with the corresponding status and error message
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException ex) {
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.UNAUTHORIZED,
                "Bad Credentials!", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handles denied access (403) errors.
     * Returns a response with a 403 status and the corresponding error message.
     *
     * @param ex denied access error
     * @return {@link ResponseEntity} object with the corresponding status and error message
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Object> handleForbiddenException(ForbiddenException ex) {
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.FORBIDDEN,
                "Forbidden!", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
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
     * Handles resource unavailable (404) errors.
     * Returns a response with a 404 status and the corresponding error message.
     *
     * @param ex resource error
     * @return {@link ResponseEntity} object with the corresponding status and error message
     */
    @ExceptionHandler(ChangeSetPersister.NotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(ChangeSetPersister.NotFoundException ex) {
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.NOT_FOUND,
                "Not Found!", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handles general exceptions (500).
     * Returns a response with status 500 and the corresponding error message.
     *
     * @param ex general exception
     * @return {@link ResponseEntity} object with the appropriate status and error message
     */
    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleInternalServerError(Exception ex) {
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error!", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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
        return new ErrorResponse(LocalDateTime.now(), message, status.value(), exceptionMessage);
    }
}
