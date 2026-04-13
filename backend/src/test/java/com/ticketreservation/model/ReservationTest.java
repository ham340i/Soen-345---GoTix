package com.ticketreservation.model;

import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for the Reservation domain model.
 */
@DisplayName("Reservation Model Unit Tests")
class ReservationTest {

    private Reservation reservation;
    private User user;
    private Event event;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L).name("Test User").email("test@test.com")
                .role(User.Role.CUSTOMER).password("encoded").build();

        event = Event.builder()
                .id(1L).name("Test Event")
                .category(Event.EventCategory.CONCERT)
                .location("Montreal").eventDate(LocalDateTime.now().plusDays(10))
                .totalSeats(200).availableSeats(200)
                .price(new BigDecimal("25.00"))
                .status(Event.EventStatus.ACTIVE).build();

        reservation = Reservation.builder()
                .id(1L).user(user).event(event)
                .numTickets(2).totalPrice(new BigDecimal("50.00"))
                .confirmationCode("TKT-ABCD1234")
                .status(Reservation.ReservationStatus.CONFIRMED)
                .build();
    }

    @Test
    @DisplayName("Should have CONFIRMED status by default")
    void newReservation_shouldBeConfirmed() {
        assertThat(reservation.getStatus()).isEqualTo(Reservation.ReservationStatus.CONFIRMED);
    }

    @Test
    @DisplayName("cancel() should set status to CANCELLED")
    void cancel_shouldUpdateStatus() {
        reservation.cancel("Changed my mind");
        assertThat(reservation.getStatus()).isEqualTo(Reservation.ReservationStatus.CANCELLED);
    }

    @Test
    @DisplayName("cancel() should record the reason")
    void cancel_shouldRecordReason() {
        reservation.cancel("Event conflict");
        assertThat(reservation.getCancellationReason()).isEqualTo("Event conflict");
    }

    @Test
    @DisplayName("cancel() should set cancelledAt timestamp")
    void cancel_shouldSetCancelledAt() {
        LocalDateTime before = LocalDateTime.now();
        reservation.cancel("Test");
        LocalDateTime after = LocalDateTime.now();

        assertThat(reservation.getCancelledAt()).isNotNull();
        assertThat(reservation.getCancelledAt()).isBetween(before, after);
    }

    @Test
    @DisplayName("Confirmation code should be non-null after build")
    void confirmationCode_shouldNotBeNull() {
        assertThat(reservation.getConfirmationCode()).isNotNull();
        assertThat(reservation.getConfirmationCode()).isNotBlank();
    }

    @Test
    @DisplayName("Total price should reflect ticket count and unit price")
    void totalPrice_shouldReflectTickets() {
        // 2 tickets * $25.00 = $50.00
        assertThat(reservation.getTotalPrice()).isEqualByComparingTo("50.00");
    }
}
