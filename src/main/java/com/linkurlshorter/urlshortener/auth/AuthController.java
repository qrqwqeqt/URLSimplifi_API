package com.linkurlshorter.urlshortener.auth;

import com.linkurlshorter.urlshortener.auth.dto.AuthRequest;
import com.linkurlshorter.urlshortener.auth.dto.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller class for handling user authentication and registration endpoints.
 *
 * <p>This class provides REST endpoints for user login and registration,
 * utilizing the {@link AuthService} for authentication and registration operations.
 *
 * @author Egor Sivenko
 * @see org.springframework.web.bind.annotation.RestController
 * @see com.linkurlshorter.urlshortener.auth.AuthService
 */
@RestController
@CrossOrigin
@RequiredArgsConstructor
@Tag(name = "Auth", description = "The Auth API")
@RequestMapping("/api/V1/auth")
public class AuthController {

    private static final String LOGIN_MESSAGE = "User logged in successfully!";
    private static final String REGISTRATION_MESSAGE = "User registered successfully!";

    private final AuthService authService;

    /**
     * Handles the POST request for user login.
     *
     * @param authRequest the authentication request containing user credentials
     * @return a ResponseEntity containing the authentication response, including a JWT token
     */
    @Operation(summary = "Log In")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequest authRequest) {
        String token = authService.loginUser(authRequest);
        return ResponseEntity.ok(new AuthResponse(LOGIN_MESSAGE, token));
    }

    /**
     * Handles the POST request for user registration.
     *
     * @param authRequest the registration request containing user credentials
     * @return a ResponseEntity containing the registration response, including a JWT token
     */
    @Operation(summary = "Registration")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid AuthRequest authRequest) {
        String token = authService.registerUser(authRequest);
        return ResponseEntity.ok(new AuthResponse(REGISTRATION_MESSAGE, token));
    }
}
