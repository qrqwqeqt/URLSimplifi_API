package com.linkurlshorter.urlshortener.exception;

import com.linkurlshorter.urlshortener.auth.exception.EmailAlreadyTakenException;
import com.linkurlshorter.urlshortener.auth.exception.WrongEmailOrPasswordException;
import com.linkurlshorter.urlshortener.link.exception.DeletedLinkException;
import com.linkurlshorter.urlshortener.link.exception.ForbiddenException;
import com.linkurlshorter.urlshortener.link.exception.InactiveLinkException;
import com.linkurlshorter.urlshortener.link.exception.InternalServerLinkException;
import com.linkurlshorter.urlshortener.link.exception.LinkStatusException;
import com.linkurlshorter.urlshortener.link.exception.NoLinkFoundByShortLinkException;
import com.linkurlshorter.urlshortener.user.exception.NoSuchEmailFoundException;
import com.linkurlshorter.urlshortener.user.exception.NoUserFoundByEmailException;
import com.linkurlshorter.urlshortener.user.exception.NoUserFoundByIdException;
import jakarta.servlet.http.HttpServletRequest;
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                Objects.requireNonNull(ex.getFieldError()).getDefaultMessage(),
                request.getRequestURI());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.UNAUTHORIZED,
                ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler({EmailAlreadyTakenException.class, LinkStatusException.class,
            DeletedLinkException.class, InactiveLinkException.class})
    public ResponseEntity<ErrorResponse> handleBadRequestExceptions(
            RuntimeException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.BAD_REQUEST,
                ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler({NoSuchEmailFoundException.class, NoUserFoundByEmailException.class,
            NoUserFoundByIdException.class, NoLinkFoundByShortLinkException.class, WrongEmailOrPasswordException.class})
    public ResponseEntity<ErrorResponse> handleNotFoundExceptions(
            RuntimeException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.NOT_FOUND,
                ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(
            ForbiddenException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.FORBIDDEN,
                ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(InternalServerLinkException.class)
    public ResponseEntity<ErrorResponse> handleInternalServerLinkException(
            InternalServerLinkException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private ErrorResponse buildErrorResponse(HttpStatus status, String message, String requestURI) {
        return new ErrorResponse(LocalDateTime.now(), status.value(), message, requestURI);
    }
}
