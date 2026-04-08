package com.ticketreservation.service;

import com.ticketreservation.exception.TicketReservationException.*;
import com.ticketreservation.model.User;
import com.ticketreservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    /**
     * Register a new customer user using email or phone number.
     * Functional Requirement: Users should be able to register using email or phone number.
     */
    public User registerUser(String name, String email, String phoneNumber, String rawPassword) {
        log.info("Registering new user with email={}, phone={}", email, phoneNumber);

        if (email != null && !email.isBlank() && userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("email", email);
        }
        if (phoneNumber != null && !phoneNumber.isBlank() && userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new UserAlreadyExistsException("phone number", phoneNumber);
        }
        if ((email == null || email.isBlank()) && (phoneNumber == null || phoneNumber.isBlank())) {
            throw new IllegalArgumentException("At least one of email or phone number must be provided");
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .password(passwordEncoder.encode(rawPassword))
                .role(User.Role.CUSTOMER)
                .build();

        User saved = userRepository.save(user);
        log.info("User registered successfully with id={}", saved.getId());

        // Send welcome notification
        try {
            notificationService.sendWelcomeNotification(saved);
        } catch (Exception e) {
            log.warn("Failed to send welcome notification to user {}: {}", saved.getId(), e.getMessage());
        }

        return saved;
    }

    /**
     * Register a new admin user.
     */
    public User registerAdmin(String name, String email, String rawPassword) {
        log.info("Registering admin user with email={}", email);

        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("email", email);
        }

        User admin = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(User.Role.ADMIN)
                .build();

        return userRepository.save(admin);
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(Long id, String name, String email, String phoneNumber) {
        User user = findById(id);

        if (name != null && !name.isBlank()) {
            user.setName(name);
        }
        if (email != null && !email.isBlank() && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new UserAlreadyExistsException("email", email);
            }
            user.setEmail(email);
        }
        if (phoneNumber != null && !phoneNumber.isBlank() && !phoneNumber.equals(user.getPhoneNumber())) {
            if (userRepository.existsByPhoneNumber(phoneNumber)) {
                throw new UserAlreadyExistsException("phone number", phoneNumber);
            }
            user.setPhoneNumber(phoneNumber);
        }

        return userRepository.save(user);
    }

    public void deactivateUser(Long id) {
        User user = findById(id);
        user.setActive(false);
        userRepository.save(user);
        log.info("User {} deactivated", id);
    }
}
