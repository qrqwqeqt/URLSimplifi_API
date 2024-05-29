package com.linkurlshorter.urlshortener.auth;

import com.linkurlshorter.urlshortener.auth.dto.AuthRequest;
import com.linkurlshorter.urlshortener.auth.exception.EmailAlreadyTakenException;
import com.linkurlshorter.urlshortener.auth.exception.WrongEmailOrPasswordException;
import com.linkurlshorter.urlshortener.jwt.JwtUtil;
import com.linkurlshorter.urlshortener.security.CustomUserDetailsService;
import com.linkurlshorter.urlshortener.user.model.User;
import com.linkurlshorter.urlshortener.user.UserRepository;
import com.linkurlshorter.urlshortener.user.model.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service class providing operations for user authentication and registration.
 *
 * <p>This class allows for user login and registration functionality,
 * including JWT token generation for authenticated users.
 *
 * @author Egor Sivenko
 * @see com.linkurlshorter.urlshortener.jwt.JwtUtil
 * @see com.linkurlshorter.urlshortener.user.UserRepository
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Authenticates a user and generates a JWT token.
     *
     * @param authRequest the authentication request containing user credentials
     * @return the JWT token generated for the authenticated user
     */
    public String loginUser(AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getEmail(), authRequest.getPassword()));

            return jwtUtil.generateToken(authentication);
        } catch (BadCredentialsException e) {
            throw new WrongEmailOrPasswordException();
        }
    }

    /**
     * Registers a new user and generates a JWT token.
     *
     * @param authRequest the registration request containing user credentials
     * @return the JWT token generated for the registered user
     * @throws EmailAlreadyTakenException if the user with the provided email is already registered
     */
    public String registerUser(AuthRequest authRequest) {
        String email = authRequest.getEmail();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyTakenException(email);
        }
        saveNewUser(authRequest);
        return loginUser(authRequest);
    }

    /**
     * Saves a new user to the database.
     *
     * <p>This method creates a new user entity using the provided authentication request,
     * sets the user's email, encrypted password, and {@link UserRole#USER USER} role, and saves the user to the database.
     *
     * @param authRequest the registration request containing user credentials
     */
    private void saveNewUser(AuthRequest authRequest) {
        User user = new User();
        user.setEmail(authRequest.getEmail());
        user.setPassword(passwordEncoder.encode(authRequest.getPassword()));
        user.setRole(UserRole.USER);

        userRepository.save(user);
    }
}
