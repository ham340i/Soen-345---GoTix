package com.ticketreservation.controller;

import com.ticketreservation.config.JwtUtil;
import com.ticketreservation.dto.AuthResponse;
import com.ticketreservation.dto.LoginRequest;
import com.ticketreservation.dto.RegisterRequest;
import com.ticketreservation.model.User;
import com.ticketreservation.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints — public (no JWT required).
 *
 * POST /api/auth/register  — register a new customer account
 * POST /api/auth/login     — authenticate and receive a JWT
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    /**
     * FR-01: Register using email or phone number.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request for email={}", request.getEmail());

        User user = userService.registerUser(
                request.getName(),
                request.getEmail(),
                request.getPhoneNumber(),
                request.getPassword()
        );

        // Auto-login: generate token immediately after registration
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails, user.getId(), user.getRole().name());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(AuthResponse.of(token, user.getId(), user.getName(),
                        user.getEmail(), user.getRole().name()));
    }

    /**
     * Authenticate with email and password, returns a Bearer JWT.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for email={}", request.getEmail());

        // Throws BadCredentialsException if invalid — caught by GlobalExceptionHandler
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userService.findByEmail(request.getEmail());
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails, user.getId(), user.getRole().name());

        return ResponseEntity.ok(
                AuthResponse.of(token, user.getId(), user.getName(),
                        user.getEmail(), user.getRole().name()));
    }
}
