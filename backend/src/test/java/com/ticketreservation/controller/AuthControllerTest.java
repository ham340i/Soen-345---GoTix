package com.ticketreservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketreservation.config.JwtUtil;
import com.ticketreservation.dto.LoginRequest;
import com.ticketreservation.dto.RegisterRequest;
import com.ticketreservation.exception.TicketReservationException.UserAlreadyExistsException;
import com.ticketreservation.model.User;
import com.ticketreservation.service.UserService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller-layer tests for AuthController using MockMvc.
 * HTTP layer only — service layer is fully mocked.
 */
@WebMvcTest(AuthController.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private UserService userService;
    @MockBean private AuthenticationManager authenticationManager;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private JwtUtil jwtUtil;

    private static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiJ9.test.signature";

    private User buildUser() {
        return User.builder()
                .id(1L).name("Alice Smith").email("alice@example.com")
                .password("$2a$12$encoded").role(User.Role.CUSTOMER).active(true).build();
    }

    private org.springframework.security.core.userdetails.UserDetails buildUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail()).password(user.getPassword())
                .roles(user.getRole().name()).build();
    }

    // ── Registration Tests ────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/auth/register")
    class RegisterTests {

        @Test
        @DisplayName("Valid registration returns 201 with token")
        void register_valid_returns201() throws Exception {
            User user = buildUser();
            given(userService.registerUser(any(), any(), any(), any())).willReturn(user);
            given(userDetailsService.loadUserByUsername(any())).willReturn(buildUserDetails(user));
            given(jwtUtil.generateToken(any(), any(), any())).willReturn(VALID_TOKEN);

            RegisterRequest req = RegisterRequest.builder()
                    .name("Alice Smith").email("alice@example.com").password("SecurePass1!").build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.token").value(VALID_TOKEN))
                    .andExpect(jsonPath("$.type").value("Bearer"))
                    .andExpect(jsonPath("$.email").value("alice@example.com"))
                    .andExpect(jsonPath("$.role").value("CUSTOMER"));
        }

        @Test
        @DisplayName("Missing name returns 400")
        void register_missingName_returns400() throws Exception {
            RegisterRequest req = RegisterRequest.builder()
                    .email("alice@example.com").password("SecurePass1!").build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"));
        }

        @Test
        @DisplayName("Password too short returns 400")
        void register_shortPassword_returns400() throws Exception {
            RegisterRequest req = RegisterRequest.builder()
                    .name("Alice").email("alice@example.com").password("abc").build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Duplicate email returns 409")
        void register_duplicateEmail_returns409() throws Exception {
            given(userService.registerUser(any(), any(), any(), any()))
                    .willThrow(new UserAlreadyExistsException("email", "alice@example.com"));

            RegisterRequest req = RegisterRequest.builder()
                    .name("Alice").email("alice@example.com").password("SecurePass1!").build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409));
        }

        @Test
        @DisplayName("Registration with phone number returns 201")
        void register_withPhone_returns201() throws Exception {
            User user = User.builder().id(2L).name("Bob").phoneNumber("+15141234567")
                    .email(null).password("encoded").role(User.Role.CUSTOMER).active(true).build();
            given(userService.registerUser(any(), isNull(), eq("+15141234567"), any())).willReturn(user);
            given(userDetailsService.loadUserByUsername(isNull())).willReturn(buildUserDetails(buildUser()));
            given(jwtUtil.generateToken(any(), any(), any())).willReturn(VALID_TOKEN);

            RegisterRequest req = RegisterRequest.builder()
                    .name("Bob").phoneNumber("+15141234567").password("SecurePass1!").build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated());
        }
    }

    // ── Login Tests ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginTests {

        @Test
        @DisplayName("Valid credentials return 200 with token")
        void login_valid_returns200() throws Exception {
            User user = buildUser();
            given(authenticationManager.authenticate(any())).willReturn(null);
            given(userService.findByEmail("alice@example.com")).willReturn(user);
            given(userDetailsService.loadUserByUsername("alice@example.com")).willReturn(buildUserDetails(user));
            given(jwtUtil.generateToken(any(), any(), any())).willReturn(VALID_TOKEN);

            LoginRequest req = LoginRequest.builder()
                    .email("alice@example.com").password("SecurePass1!").build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value(VALID_TOKEN))
                    .andExpect(jsonPath("$.userId").value(1));
        }

        @Test
        @DisplayName("Wrong password returns 401")
        void login_wrongPassword_returns401() throws Exception {
            given(authenticationManager.authenticate(any()))
                    .willThrow(new BadCredentialsException("Bad credentials"));

            LoginRequest req = LoginRequest.builder()
                    .email("alice@example.com").password("WrongPass!").build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Invalid email or password"));
        }

        @Test
        @DisplayName("Missing email returns 400")
        void login_missingEmail_returns400() throws Exception {
            LoginRequest req = LoginRequest.builder().password("pass").build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }
    }
}
