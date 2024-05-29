package com.linkurlshorter.urlshortener.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkurlshorter.urlshortener.auth.dto.AuthRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the AuthController class.
 *
 * @author Ivan Shalaiev
 * @version 1.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@Testcontainers
@Transactional
@Rollback
class AuthControllerIntegrationTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:16.0-alpine");

    @Autowired
    private MockMvc mockMvc;
    private final String baseUrl = "/api/V1/auth/";
    private AuthRequest authRequest;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Test case to verify successful user login.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    void loginSuccessfulTest() throws Exception {
        authRequest = new AuthRequest("user1@example.com", "Pass1234");
        this.mockMvc.perform(MockMvcRequestBuilders.post(baseUrl + "login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("User logged in successfully!"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.jwtToken").exists());
    }

    /**
     * Test case to verify login failure when user does not exist.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    void loginFailedWhenUserDoesNotExistTest() throws Exception {
        authRequest = new AuthRequest("userNotFound@example.com", "Pass1234");
        this.mockMvc.perform(MockMvcRequestBuilders.post(baseUrl + "login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode").value(404))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Wrong email or password"));
    }

    /**
     * Test case to verify login failure when password does not match.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    void loginFailedWhenPasswordDoesNotMatchTest() throws Exception {
        authRequest = new AuthRequest("user1@example.com", "Pass12345");
        this.mockMvc.perform(MockMvcRequestBuilders.post(baseUrl + "login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode").value(404))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Wrong email or password"));
    }

    /**
     * Parameterized test to verify login failure with invalid passwords.
     *
     * @param password the password to test
     * @throws Exception if any error occurs during the test
     */
    @ParameterizedTest
    @ValueSource(strings = {"", "Password", "Pass123", "pass1234", "Pass 1234", "PASS1234"})
    void loginFailedWhenInvalidPasswordGivenTest(String password) throws Exception {
        authRequest = new AuthRequest("user1@example.com", password);
        mockMvc.perform(post(baseUrl + "login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode").value(400))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Password " +
                        "must be at least 8 characters long and contain at least one digit, one uppercase letter, " +
                        "and one lowercase letter. No spaces are allowed."));
    }

    /**
     * Parameterized test to verify login failure with invalid emails.
     *
     * @param email the email address to test
     * @throws Exception if any error occurs during the test
     */
    @ParameterizedTest
    @ValueSource(strings = {"", "user test@example.com",
            " user-test@example.com",
            "user-test%@example.com",
            "user-test#@example.com",
            "user-test.example.com",
            "user-test@example"})
    void loginFailedWhenInvalidEmailGivenTest(String email) throws Exception {
        authRequest = new AuthRequest(email, "Pass1234");
        mockMvc.perform(post(baseUrl + "login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode").value(400))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Email address entered incorrectly!"));
    }

    /**
     * Test case to verify successful user registration.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    void registerSuccessfulTest() throws Exception {
        authRequest = new AuthRequest("user-test@example.com", "Pass1234");
        this.mockMvc.perform(MockMvcRequestBuilders.post(baseUrl + "register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("User registered successfully!"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.jwtToken").exists());
    }

    /**
     * Parameterized test to verify registration failure with invalid passwords.
     *
     * @param password the password to test
     * @throws Exception if any error occurs during the test
     */
    @ParameterizedTest
    @ValueSource(strings = {"", "Password", "Pass123", "pass1234", "Pass 1234", "PASS1234"})
    void registerFailedWhenInvalidPasswordGivenTest(String password) throws Exception {
        authRequest = new AuthRequest("user-test@example.com", password);
        mockMvc.perform(post(baseUrl + "register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode").value(400))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Password " +
                        "must be at least 8 characters long and contain at least one digit, one uppercase letter, " +
                        "and one lowercase letter. No spaces are allowed."));
    }

    /**
     * Parameterized test to verify registration failure with invalid emails.
     *
     * @param email the email address to test
     * @throws Exception if any error occurs during the test
     */
    @ParameterizedTest
    @ValueSource(strings = {"", "user test@example.com",
            " user-test@example.com",
            "user-test%@example.com",
            "user-test#@example.com",
            "user-test.example.com",
            "user-test@example"})
    void registerFailedWhenInvalidEmailGivenTest(String email) throws Exception {
        authRequest = new AuthRequest(email, "Pass1234");
        mockMvc.perform(post(baseUrl + "register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode").value(400))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Email address entered incorrectly!"));
    }
}
