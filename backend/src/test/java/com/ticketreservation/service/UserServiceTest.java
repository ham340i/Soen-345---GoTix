package com.ticketreservation.service;

import com.ticketreservation.exception.TicketReservationException.*;
import com.ticketreservation.model.User;
import com.ticketreservation.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Unit tests for UserService.
 * Covers: registration, validation, retrieval, update.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private UserService userService;

    private static final String VALID_NAME = "Alice Smith";
    private static final String VALID_EMAIL = "alice@example.com";
    private static final String VALID_PHONE = "+15141234567";
    private static final String VALID_PASSWORD = "SecurePass123!";
    private static final String ENCODED_PASSWORD = "$2a$10$hashedpassword";

    @Nested
    @DisplayName("User Registration")
    class RegisterUserTests {

        @Test
        @DisplayName("Should register user with email successfully")
        void registerUser_withEmail_shouldSucceed() {
            given(userRepository.existsByEmail(VALID_EMAIL)).willReturn(false);
            given(passwordEncoder.encode(VALID_PASSWORD)).willReturn(ENCODED_PASSWORD);
            User saved = buildUser(1L, VALID_NAME, VALID_EMAIL, null);
            given(userRepository.save(any(User.class))).willReturn(saved);

            User result = userService.registerUser(VALID_NAME, VALID_EMAIL, null, VALID_PASSWORD);

            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(VALID_EMAIL);
            then(userRepository).should().save(any(User.class));
            then(notificationService).should().sendWelcomeNotification(any(User.class));
        }

        @Test
        @DisplayName("Should register user with phone number successfully")
        void registerUser_withPhone_shouldSucceed() {
            given(userRepository.existsByPhoneNumber(VALID_PHONE)).willReturn(false);
            given(passwordEncoder.encode(VALID_PASSWORD)).willReturn(ENCODED_PASSWORD);
            User saved = buildUser(2L, VALID_NAME, null, VALID_PHONE);
            given(userRepository.save(any(User.class))).willReturn(saved);

            User result = userService.registerUser(VALID_NAME, null, VALID_PHONE, VALID_PASSWORD);

            assertThat(result).isNotNull();
            assertThat(result.getPhoneNumber()).isEqualTo(VALID_PHONE);
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void registerUser_duplicateEmail_shouldThrow() {
            given(userRepository.existsByEmail(VALID_EMAIL)).willReturn(true);

            assertThatThrownBy(() ->
                    userService.registerUser(VALID_NAME, VALID_EMAIL, null, VALID_PASSWORD))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .hasMessageContaining("email");
        }

        @Test
        @DisplayName("Should throw exception when phone already exists")
        void registerUser_duplicatePhone_shouldThrow() {
            given(userRepository.existsByPhoneNumber(VALID_PHONE)).willReturn(true);

            assertThatThrownBy(() ->
                    userService.registerUser(VALID_NAME, null, VALID_PHONE, VALID_PASSWORD))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .hasMessageContaining("phone number");
        }

        @Test
        @DisplayName("Should throw exception when neither email nor phone provided")
        void registerUser_noContact_shouldThrow() {
            assertThatThrownBy(() ->
                    userService.registerUser(VALID_NAME, null, null, VALID_PASSWORD))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("At least one");
        }

        @Test
        @DisplayName("Should assign CUSTOMER role by default")
        void registerUser_shouldAssignCustomerRole() {
            given(userRepository.existsByEmail(VALID_EMAIL)).willReturn(false);
            given(passwordEncoder.encode(VALID_PASSWORD)).willReturn(ENCODED_PASSWORD);
            User saved = buildUser(1L, VALID_NAME, VALID_EMAIL, null);
            given(userRepository.save(any(User.class))).willReturn(saved);

            User result = userService.registerUser(VALID_NAME, VALID_EMAIL, null, VALID_PASSWORD);

            assertThat(result.getRole()).isEqualTo(User.Role.CUSTOMER);
        }

        @Test
        @DisplayName("Should encode password before saving")
        void registerUser_shouldEncodePassword() {
            given(userRepository.existsByEmail(VALID_EMAIL)).willReturn(false);
            given(passwordEncoder.encode(VALID_PASSWORD)).willReturn(ENCODED_PASSWORD);
            given(userRepository.save(any(User.class))).willReturn(buildUser(1L, VALID_NAME, VALID_EMAIL, null));

            userService.registerUser(VALID_NAME, VALID_EMAIL, null, VALID_PASSWORD);

            then(passwordEncoder).should().encode(VALID_PASSWORD);
        }

        @Test
        @DisplayName("Should continue registration even if welcome notification fails")
        void registerUser_notificationFailure_shouldStillRegister() {
            given(userRepository.existsByEmail(VALID_EMAIL)).willReturn(false);
            given(passwordEncoder.encode(VALID_PASSWORD)).willReturn(ENCODED_PASSWORD);
            User saved = buildUser(1L, VALID_NAME, VALID_EMAIL, null);
            given(userRepository.save(any(User.class))).willReturn(saved);
            willThrow(new RuntimeException("SMTP error")).given(notificationService).sendWelcomeNotification(any());

            // Should NOT throw
            assertThatNoException().isThrownBy(() ->
                    userService.registerUser(VALID_NAME, VALID_EMAIL, null, VALID_PASSWORD));
        }
    }

    @Nested
    @DisplayName("Find User")
    class FindUserTests {

        @Test
        @DisplayName("Should find user by id")
        void findById_existingUser_shouldReturn() {
            User user = buildUser(1L, VALID_NAME, VALID_EMAIL, null);
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            User result = userService.findById(1L);

            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw when user not found by id")
        void findById_notFound_shouldThrow() {
            given(userRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.findById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should find user by email")
        void findByEmail_existingUser_shouldReturn() {
            User user = buildUser(1L, VALID_NAME, VALID_EMAIL, null);
            given(userRepository.findByEmail(VALID_EMAIL)).willReturn(Optional.of(user));

            User result = userService.findByEmail(VALID_EMAIL);

            assertThat(result.getEmail()).isEqualTo(VALID_EMAIL);
        }
    }

    @Nested
    @DisplayName("Admin Registration")
    class RegisterAdminTests {

        @Test
        @DisplayName("Should register admin with ADMIN role")
        void registerAdmin_shouldAssignAdminRole() {
            given(userRepository.existsByEmail(VALID_EMAIL)).willReturn(false);
            given(passwordEncoder.encode(VALID_PASSWORD)).willReturn(ENCODED_PASSWORD);
            User admin = User.builder().id(1L).name(VALID_NAME).email(VALID_EMAIL)
                    .role(User.Role.ADMIN).password(ENCODED_PASSWORD).build();
            given(userRepository.save(any(User.class))).willReturn(admin);

            User result = userService.registerAdmin(VALID_NAME, VALID_EMAIL, VALID_PASSWORD);

            assertThat(result.getRole()).isEqualTo(User.Role.ADMIN);
        }
    }

    // ---- Helpers ----
    private User buildUser(Long id, String name, String email, String phone) {
        return User.builder()
                .id(id).name(name).email(email).phoneNumber(phone)
                .password(ENCODED_PASSWORD).role(User.Role.CUSTOMER).active(true).build();
    }
}
