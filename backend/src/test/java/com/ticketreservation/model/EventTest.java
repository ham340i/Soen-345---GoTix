package com.ticketreservation.model;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for the Event domain model.
 * Covers seat management logic and state transitions.
 */
@DisplayName("Event Model Unit Tests")
class EventTest {

    private Event event;

    @BeforeEach
    void setUp() {
        event = Event.builder()
                .id(1L)
                .name("Summer Concert")
                .category(Event.EventCategory.CONCERT)
                .location("Montreal Bell Centre")
                .eventDate(LocalDateTime.now().plusDays(30))
                .totalSeats(100)
                .availableSeats(100)
                .price(new BigDecimal("49.99"))
                .status(Event.EventStatus.ACTIVE)
                .build();
    }

    @Nested
    @DisplayName("Seat Availability")
    class SeatAvailabilityTests {

        @Test
        @DisplayName("Should return true when enough seats are available")
        void hasAvailableSeats_sufficient_returnsTrue() {
            assertThat(event.hasAvailableSeats(50)).isTrue();
        }

        @Test
        @DisplayName("Should return true when exactly the available seats are requested")
        void hasAvailableSeats_exactAmount_returnsTrue() {
            assertThat(event.hasAvailableSeats(100)).isTrue();
        }

        @Test
        @DisplayName("Should return false when more seats are requested than available")
        void hasAvailableSeats_insufficient_returnsFalse() {
            assertThat(event.hasAvailableSeats(101)).isFalse();
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 10, 50, 99, 100})
        @DisplayName("Should return true for any valid seat count within limit")
        void hasAvailableSeats_validCounts_returnTrue(int seats) {
            assertThat(event.hasAvailableSeats(seats)).isTrue();
        }
    }

    @Nested
    @DisplayName("Reserve Seats")
    class ReserveSeatsTests {

        @Test
        @DisplayName("Should decrement available seats correctly")
        void reserveSeats_valid_decrementsAvailable() {
            event.reserveSeats(10);
            assertThat(event.getAvailableSeats()).isEqualTo(90);
        }

        @Test
        @DisplayName("Should allow reserving all seats")
        void reserveSeats_allSeats_zeroRemaining() {
            event.reserveSeats(100);
            assertThat(event.getAvailableSeats()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should throw when reserving more than available")
        void reserveSeats_tooMany_throwsException() {
            assertThatThrownBy(() -> event.reserveSeats(101))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Not enough available seats");
        }

        @Test
        @DisplayName("Should handle sequential reservations correctly")
        void reserveSeats_sequential_cumulativeDecrement() {
            event.reserveSeats(30);
            event.reserveSeats(20);
            assertThat(event.getAvailableSeats()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("Release Seats")
    class ReleaseSeatsTests {

        @Test
        @DisplayName("Should increment available seats on release")
        void releaseSeats_valid_incrementsAvailable() {
            event.reserveSeats(20);
            event.releaseSeats(20);
            assertThat(event.getAvailableSeats()).isEqualTo(100);
        }

        @Test
        @DisplayName("Should not exceed total seats on release")
        void releaseSeats_overshoot_capsAtTotalSeats() {
            event.releaseSeats(50); // releasing from full capacity
            assertThat(event.getAvailableSeats()).isEqualTo(100); // capped at total
        }

        @Test
        @DisplayName("Should partially release seats correctly")
        void releaseSeats_partial_correctAvailability() {
            event.reserveSeats(50);
            event.releaseSeats(20);
            assertThat(event.getAvailableSeats()).isEqualTo(70);
        }
    }

    @Nested
    @DisplayName("Event Status")
    class EventStatusTests {

        @Test
        @DisplayName("New events should have ACTIVE status")
        void newEvent_shouldBeActive() {
            assertThat(event.getStatus()).isEqualTo(Event.EventStatus.ACTIVE);
        }

        @Test
        @DisplayName("Event categories should be correctly assignable")
        void eventCategory_allValuesValid() {
            for (Event.EventCategory cat : Event.EventCategory.values()) {
                event.setCategory(cat);
                assertThat(event.getCategory()).isEqualTo(cat);
            }
        }
    }
}
