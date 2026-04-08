package com.ticketreservation.controller;

import com.ticketreservation.config.JwtUtil;
import com.ticketreservation.model.User;
import com.ticketreservation.service.UserService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User profile and admin user-management endpoints.
 *
 * GET  /api/users/me                   — get own profile
 * PUT  /api/users/me                   — update own profile
 * GET  /api/users        (ADMIN)       — list all users
 * GET  /api/users/{id}  (ADMIN)        — get any user
 * DELETE /api/users/{id} (ADMIN)       — deactivate user
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /** Get the currently authenticated user's profile. */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getProfile(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(UserResponse.from(userService.findById(userId)));
    }

    /** Update the currently authenticated user's name, email, or phone. */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UpdateProfileRequest request) {
        Long userId = extractUserId(authHeader);
        User updated = userService.updateUser(
                userId, request.getName(), request.getEmail(), request.getPhoneNumber());
        return ResponseEntity.ok(UserResponse.from(updated));
    }

    /** ADMIN: List all registered users. */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.findAllUsers().stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    /** ADMIN: Get any user by ID. */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(UserResponse.from(userService.findById(id)));
    }

    /** ADMIN: Deactivate a user account. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    // ── Inner DTOs (kept here to avoid separate files for small payloads) ──────

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UserResponse {
        private Long id;
        private String name;
        private String email;
        private String phoneNumber;
        private String role;
        private boolean active;
        private LocalDateTime createdAt;

        public static UserResponse from(User user) {
            return UserResponse.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .phoneNumber(user.getPhoneNumber())
                    .role(user.getRole().name())
                    .active(user.isActive())
                    .createdAt(user.getCreatedAt())
                    .build();
        }
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class UpdateProfileRequest {
        private String name;
        private String email;
        private String phoneNumber;
    }

    private Long extractUserId(String authHeader) {
        return jwtUtil.extractUserId(authHeader.substring(7));
    }
}
