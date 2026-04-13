package com.ticketreservation.service;

import com.ticketreservation.exception.TicketReservationException.*;
import com.ticketreservation.model.Event;
import com.ticketreservation.model.Reservation;
import com.ticketreservation.model.User;
import com.ticketreservation.repository.EventRepository;
import com.ticketreservation.repository.ReservationRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Unit tests for ReservationService.
 * Covers: reservation creation, cancellation, edge cases.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationService Unit Tests")
class ReservationServiceTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private EventRepository eventRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private ReservationService reservationService;

    private User customer;
    private User admin;
    private Event activeEvent;

    @BeforeEach
    void setUp() {
        customer = User.builder().id(1L).name("Bob").email("bob@test.com")
                .role(User.Role.CUSTOMER).password("encoded").build();
        admin = User.builder().id(2L).name("Admin").email("admin@test.com")
                .role(User.Role.ADMIN).password("encoded").build();
        activeEvent = buildEvent(1L, "Jazz Festival", Event.EventStatus.ACTIVE, 200, 200);
    }

    @Nested
    @DisplayName("Create Reservation")
    class CreateReservationTests {

        @Test
        @DisplayName("Should create reservation successfully")
        void createReservation_valid_shouldSucceed() {
            given(eventRepository.findById(1L)).willReturn(Optional.of(activeEvent));
            Reservation saved = buildReservation(1L, activeEvent, customer, 2, Reservation.ReservationStatus.CONFIRMED);
            given(reservationRepository.save(any(Reservation.class))).willReturn(saved);
            given(eventRepository.save(any(Event.class))).willReturn(activeEvent);
            willDoNothing().given(notificationService).sendReservationConfirmation(any());

            Reservation result = reservationService.createReservation(customer, 1L, 2);

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(Reservation.ReservationStatus.CONFIRMED);
            assertThat(result.getNumTickets()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should calculate total price correctly")
        void createReservation_shouldCalculateTotalPrice() {
            // $49.99 x 3 = $149.97
            given(eventRepository.findById(1L)).willReturn(Optional.of(activeEvent));
            Reservation saved = buildReservation(1L, activeEvent, customer, 3, Reservation.ReservationStatus.CONFIRMED);
            saved.setTotalPrice(new BigDecimal("149.97"));
            given(reservationRepository.save(any(Reservation.class))).willReturn(saved);
            given(eventRepository.save(any(Event.class))).willReturn(activeEvent);
            willDoNothing().given(notificationService).sendReservationConfirmation(any());

            Reservation result = reservationService.createReservation(customer, 1L, 3);

            assertThat(result.getTotalPrice()).isEqualByComparingTo("149.97");
        }

        @Test
        @DisplayName("Should throw when event is cancelled")
        void createReservation_cancelledEvent_shouldThrow() {
            Event cancelled = buildEvent(1L, "Show", Event.EventStatus.CANCELLED, 100, 100);
            given(eventRepository.findById(1L)).willReturn(Optional.of(cancelled));

            assertThatThrownBy(() -> reservationService.createReservation(customer, 1L, 1))
                    .isInstanceOf(EventCancelledException.class);
        }

        @Test
        @DisplayName("Should throw when not enough seats available")
        void createReservation_insufficientSeats_shouldThrow() {
            Event almostFull = buildEvent(1L, "Concert", Event.EventStatus.ACTIVE, 100, 1);
            given(eventRepository.findById(1L)).willReturn(Optional.of(almostFull));

            assertThatThrownBy(() -> reservationService.createReservation(customer, 1L, 5))
                    .isInstanceOf(InsufficientSeatsException.class)
                    .hasMessageContaining("Not enough seats");
        }

        @Test
        @DisplayName("Should throw when event not found")
        void createReservation_eventNotFound_shouldThrow() {
            given(eventRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.createReservation(customer, 99L, 1))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -10})
        @DisplayName("Should throw when tickets count is not positive")
        void createReservation_invalidTicketCount_shouldThrow(int tickets) {
            assertThatThrownBy(() -> reservationService.createReservation(customer, 1L, tickets))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("at least 1");
        }

        @Test
        @DisplayName("Should decrement event available seats")
        void createReservation_shouldDecrementSeats() {
            Event event = buildEvent(1L, "Concert", Event.EventStatus.ACTIVE, 100, 100);
            given(eventRepository.findById(1L)).willReturn(Optional.of(event));
            given(reservationRepository.save(any())).willReturn(buildReservation(1L, event, customer, 5, Reservation.ReservationStatus.CONFIRMED));
            given(eventRepository.save(any(Event.class))).willReturn(event);
            willDoNothing().given(notificationService).sendReservationConfirmation(any());

            reservationService.createReservation(customer, 1L, 5);

            assertThat(event.getAvailableSeats()).isEqualTo(95);
        }

        @Test
        @DisplayName("Should still succeed if notification fails")
        void createReservation_notificationFails_shouldStillSave() {
            given(eventRepository.findById(1L)).willReturn(Optional.of(activeEvent));
            given(reservationRepository.save(any())).willReturn(buildReservation(1L, activeEvent, customer, 1, Reservation.ReservationStatus.CONFIRMED));
            given(eventRepository.save(any())).willReturn(activeEvent);
            willThrow(new RuntimeException("SMTP down")).given(notificationService).sendReservationConfirmation(any());

            assertThatNoException().isThrownBy(() -> reservationService.createReservation(customer, 1L, 1));
        }
    }

    @Nested
    @DisplayName("Cancel Reservation")
    class CancelReservationTests {

        @Test
        @DisplayName("Owner can cancel their own reservation")
        void cancelReservation_byOwner_shouldSucceed() {
            Reservation reservation = buildReservation(1L, activeEvent, customer, 2, Reservation.ReservationStatus.CONFIRMED);
            given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
            given(eventRepository.save(any())).willReturn(activeEvent);
            given(reservationRepository.save(any())).willReturn(reservation);
            willDoNothing().given(notificationService).sendCancellationNotification(any());

            Reservation result = reservationService.cancelReservation(1L, customer);

            assertThat(result.getStatus()).isEqualTo(Reservation.ReservationStatus.CANCELLED);
        }

        @Test
        @DisplayName("Admin can cancel any reservation")
        void cancelReservation_byAdmin_shouldSucceed() {
            Reservation reservation = buildReservation(1L, activeEvent, customer, 2, Reservation.ReservationStatus.CONFIRMED);
            given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
            given(eventRepository.save(any())).willReturn(activeEvent);
            given(reservationRepository.save(any())).willReturn(reservation);
            willDoNothing().given(notificationService).sendCancellationNotification(any());

            assertThatNoException().isThrownBy(() -> reservationService.cancelReservation(1L, admin));
        }

        @Test
        @DisplayName("Should throw when unauthorized user tries to cancel")
        void cancelReservation_unauthorized_shouldThrow() {
            User other = User.builder().id(99L).name("Hacker").role(User.Role.CUSTOMER).password("x").build();
            Reservation reservation = buildReservation(1L, activeEvent, customer, 2, Reservation.ReservationStatus.CONFIRMED);
            given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

            assertThatThrownBy(() -> reservationService.cancelReservation(1L, other))
                    .isInstanceOf(UnauthorizedAccessException.class);
        }

        @Test
        @DisplayName("Should throw when reservation already cancelled")
        void cancelReservation_alreadyCancelled_shouldThrow() {
            Reservation reservation = buildReservation(1L, activeEvent, customer, 2, Reservation.ReservationStatus.CANCELLED);
            given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

            assertThatThrownBy(() -> reservationService.cancelReservation(1L, customer))
                    .isInstanceOf(ReservationAlreadyCancelledException.class);
        }

        @Test
        @DisplayName("Should release seats back to event on cancellation")
        void cancelReservation_shouldReleaseSeats() {
            Event event = buildEvent(1L, "Concert", Event.EventStatus.ACTIVE, 100, 98);
            Reservation reservation = buildReservation(1L, event, customer, 2, Reservation.ReservationStatus.CONFIRMED);
            given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
            given(eventRepository.save(any())).willReturn(event);
            given(reservationRepository.save(any())).willReturn(reservation);
            willDoNothing().given(notificationService).sendCancellationNotification(any());

            reservationService.cancelReservation(1L, customer);

            assertThat(event.getAvailableSeats()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("Get User Reservations")
    class GetReservationsTests {

        @Test
        @DisplayName("Should return all reservations for a user")
        void getUserReservations_shouldReturnList() {
            List<Reservation> reservations = List.of(
                    buildReservation(1L, activeEvent, customer, 2, Reservation.ReservationStatus.CONFIRMED),
                    buildReservation(2L, activeEvent, customer, 1, Reservation.ReservationStatus.CANCELLED));
            given(reservationRepository.findByUserIdOrderByReservedAtDesc(1L)).willReturn(reservations);

            List<Reservation> result = reservationService.getUserReservations(1L);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should find reservation by confirmation code")
        void findByConfirmationCode_shouldReturn() {
            Reservation reservation = buildReservation(1L, activeEvent, customer, 2, Reservation.ReservationStatus.CONFIRMED);
            reservation.setConfirmationCode("TKT-XYZ123");
            given(reservationRepository.findByConfirmationCode("TKT-XYZ123")).willReturn(Optional.of(reservation));

            Reservation result = reservationService.findByConfirmationCode("TKT-XYZ123");

            assertThat(result.getConfirmationCode()).isEqualTo("TKT-XYZ123");
        }
    }

    // ---- Helpers ----
    private Event buildEvent(Long id, String name, Event.EventStatus status, int total, int available) {
        return Event.builder()
                .id(id).name(name).status(status)
                .category(Event.EventCategory.CONCERT)
                .location("Montreal").eventDate(LocalDateTime.now().plusDays(30))
                .totalSeats(total).availableSeats(available)
                .price(new BigDecimal("49.99")).build();
    }

    private Reservation buildReservation(Long id, Event event, User user, int tickets, Reservation.ReservationStatus status) {
        return Reservation.builder()
                .id(id).event(event).user(user)
                .numTickets(tickets)
                .totalPrice(event.getPrice().multiply(BigDecimal.valueOf(tickets)))
                .confirmationCode("TKT-TEST01").status(status).build();
    }
}
