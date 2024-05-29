package com.linkurlshorter.urlshortener.user;

import com.linkurlshorter.urlshortener.auth.exception.EmailAlreadyTakenException;
import com.linkurlshorter.urlshortener.jwt.JwtUtil;
import com.linkurlshorter.urlshortener.security.CustomUserDetailsService;
import com.linkurlshorter.urlshortener.user.exception.NoSuchEmailFoundException;
import com.linkurlshorter.urlshortener.user.model.User;
import com.linkurlshorter.urlshortener.user.request.ChangeUserEmailRequest;
import com.linkurlshorter.urlshortener.user.request.ChangeUserPasswordRequest;
import com.linkurlshorter.urlshortener.user.responce.UserModifyingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller class for handling user-related operations.
 *
 * <p>
 * This class defines REST endpoints for changing user passwords and email addresses.
 * </p>
 *
 * @author Artem Poliakov
 * @version 1.0
 */
@RestController
@CrossOrigin
@RequiredArgsConstructor
@Tag(name = "User", description = "The User API")
@RequestMapping("/api/V1/user")
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtil jwtUtil;

    /**
     * Handles POST requests to change a user's password.
     *
     * @param passRequest the request payload containing the new password.
     * @return a {@link ResponseEntity} indicating the result of the operation.
     * @throws NoSuchEmailFoundException if the user's email is not found.
     */
    @PostMapping("/change-password")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Change user password")
    public ResponseEntity<UserModifyingResponse> changePassword(@RequestBody @Valid ChangeUserPasswordRequest passRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int alteredCount = userService.updateByEmailDynamically(
                User.builder()
                        .password(passwordEncoder.encode(passRequest.getNewPassword()))
                        .build(),
                authentication.getName()
        );
        if (alteredCount <= 0) {
            throw new NoSuchEmailFoundException();
        } else {
            UserModifyingResponse response = new UserModifyingResponse("ok", null);
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Handles POST requests to change a user's email address.
     *
     * @param emailRequest the request payload containing the new email address.
     * @return a {@link ResponseEntity} indicating the result of the operation, including a new JWT token.
     * @throws NoSuchEmailFoundException if the user's email is not found.
     */
    @PostMapping("/change-email")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Change user email")
    public ResponseEntity<UserModifyingResponse> changeEmail(@RequestBody @Valid ChangeUserEmailRequest emailRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String newEmail = emailRequest.getNewEmail();
        if (userService.existsByEmail(newEmail)) {
            throw new EmailAlreadyTakenException(newEmail);
        }
        int alteredCount = userService.updateByEmailDynamically(
                User.builder()
                        .email(newEmail)
                        .build(),
                authentication.getName()
        );
        if (alteredCount <= 0) {
            throw new NoSuchEmailFoundException();
        } else {
            String refreshedToken = getRefreshedToken(newEmail);
            UserModifyingResponse response = new UserModifyingResponse("ok", refreshedToken);
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Generates a new JWT token after successfully updating the user's email address.
     *
     * @param newEmail The new email address of the user.
     * @return The newly generated JWT token with updated user details.
     */
    private String getRefreshedToken(String newEmail) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(newEmail);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userDetails.getUsername(), userDetails.getPassword(), userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        return jwtUtil.generateToken(authenticationToken);
    }
}
