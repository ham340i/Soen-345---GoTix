package com.ticketreservation.service;

import com.ticketreservation.exception.TicketReservationException.*;
import com.ticketreservation.model.Event;
import com.ticketreservation.model.Reservation;
import com.ticketreservation.model.User;
import com.ticketreservation.repository.EventRepository;
import com.ticketreservation.repository.ReservationRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
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
 * Unit tests for EventService.
 * Covers: event creation, update, cancellation, search, filter.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EventService Unit Tests")
class EventServiceTest {

    @Mock private EventRepository eventRepository;
    @Mock private ReservationRepository reservationRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private EventService eventService;

    private User adminUser;
    private LocalDateTime futureDate;

    @BeforeEach
    void setUp() {
        adminUser = User.builder().id(1L).name("Admin").email("admin@test.com")
                .role(User.Role.ADMIN).password("encoded").build();
        futureDate = LocalDateTime.now().plusDays(30);
    }

    @Nested
    @DisplayName("Create Event")
    class CreateEventTests {

        @Test
        @DisplayName("Should create event successfully")
        void createEvent_valid_shouldSucceed() {
            Event saved = buildEvent(1L, "Rock Concert", Event.EventStatus.ACTIVE, 200, 200);
            given(eventRepository.save(any(Event.class))).willReturn(saved);

            Event result = eventService.createEvent("Rock Concert", "Great show",
                    Event.EventCategory.CONCERT, "Montreal", futureDate, 200,
                    new BigDecimal("49.99"), adminUser);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Rock Concert");
            then(eventRepository).should().save(any(Event.class));
        }

        @Test
        @DisplayName("Should throw when event date is in the past")
        void createEvent_pastDate_shouldThrow() {
            LocalDateTime pastDate = LocalDateTime.now().minusDays(1);

            assertThatThrownBy(() ->
                    eventService.createEvent("Old Show", "desc", Event.EventCategory.CONCERT,
                            "Montreal", pastDate, 100, new BigDecimal("20.00"), adminUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("future");
        }

        @Test
        @DisplayName("Should throw when total seats is zero")
        void createEvent_zeroSeats_shouldThrow() {
            assertThatThrownBy(() ->
                    eventService.createEvent("Show", "desc", Event.EventCategory.CONCERT,
                            "Montreal", futureDate, 0, new BigDecimal("20.00"), adminUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("Should set available seats equal to total seats on creation")
        void createEvent_shouldSetAvailableSeats() {
            Event saved = buildEvent(1L, "Concert", Event.EventStatus.ACTIVE, 500, 500);
            given(eventRepository.save(any(Event.class))).willReturn(saved);

            Event result = eventService.createEvent("Concert", "desc", Event.EventCategory.CONCERT,
                    "Montreal", futureDate, 500, new BigDecimal("99.99"), adminUser);

            assertThat(result.getAvailableSeats()).isEqualTo(result.getTotalSeats());
        }
    }

    @Nested
    @DisplayName("Cancel Event")
    class CancelEventTests {

        @Test
        @DisplayName("Should cancel active event and notify users")
        void cancelEvent_activeEvent_shouldCancel() {
            Event event = buildEvent(1L, "Concert", Event.EventStatus.ACTIVE, 100, 80);
            given(eventRepository.findById(1L)).willReturn(Optional.of(event));
            given(eventRepository.save(any(Event.class))).willReturn(event);

            Reservation r = buildReservation(1L, event, adminUser, Reservation.ReservationStatus.CONFIRMED);
            given(reservationRepository.findByEventIdOrderByReservedAtDesc(1L)).willReturn(List.of(r));
            given(reservationRepository.save(any(Reservation.class))).willReturn(r);
            willDoNothing().given(notificationService).sendCancellationNotification(any());

            Event cancelled = eventService.cancelEvent(1L);

            assertThat(cancelled.getStatus()).isEqualTo(Event.EventStatus.CANCELLED);
        }

        @Test
        @DisplayName("Should throw when cancelling already-cancelled event")
        void cancelEvent_alreadyCancelled_shouldThrow() {
            Event event = buildEvent(1L, "Concert", Event.EventStatus.CANCELLED, 100, 100);
            given(eventRepository.findById(1L)).willReturn(Optional.of(event));

            assertThatThrownBy(() -> eventService.cancelEvent(1L))
                    .isInstanceOf(EventCancelledException.class);
        }

        @Test
        @DisplayName("Should throw when event not found")
        void cancelEvent_notFound_shouldThrow() {
            given(eventRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> eventService.cancelEvent(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Filter & Search Events")
    class FilterSearchTests {

        @Test
        @DisplayName("Should return active events")
        void getAvailableEvents_shouldReturnActive() {
            List<Event> events = List.of(
                    buildEvent(1L, "Concert", Event.EventStatus.ACTIVE, 100, 50),
                    buildEvent(2L, "Movie", Event.EventStatus.ACTIVE, 200, 150));
            given(eventRepository.findByStatusOrderByEventDateAsc(Event.EventStatus.ACTIVE))
                    .willReturn(events);

            List<Event> result = eventService.getAvailableEvents();

            assertThat(result).hasSize(2);
            assertThat(result).allMatch(e -> e.getStatus() == Event.EventStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should filter events by category and location")
        void filterEvents_byCategory_shouldReturn() {
            List<Event> events = List.of(buildEvent(1L, "Concert", Event.EventStatus.ACTIVE, 100, 100));
            given(eventRepository.filterEvents(Event.EventCategory.CONCERT, "Montreal", null, null))
                    .willReturn(events);

            List<Event> result = eventService.filterEvents(Event.EventCategory.CONCERT, "Montreal", null, null);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should search events by keyword")
        void searchEvents_shouldReturnMatches() {
            List<Event> events = List.of(buildEvent(1L, "Jazz Concert", Event.EventStatus.ACTIVE, 100, 100));
            given(eventRepository.searchEvents("jazz")).willReturn(events);

            List<Event> result = eventService.searchEvents("jazz");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).containsIgnoringCase("jazz");
        }
    }

    @Nested
    @DisplayName("Update Event")
    class UpdateEventTests {

        @Test
        @DisplayName("Should update event name and location")
        void updateEvent_validFields_shouldUpdate() {
            Event event = buildEvent(1L, "Old Name", Event.EventStatus.ACTIVE, 100, 100);
            given(eventRepository.findById(1L)).willReturn(Optional.of(event));
            given(eventRepository.save(any(Event.class))).willReturn(event);

            Event result = eventService.updateEvent(1L, "New Name", null, "New Location", null, null, 0);

            assertThat(result.getName()).isEqualTo("New Name");
        }

        @Test
        @DisplayName("Should throw when updating cancelled event")
        void updateEvent_cancelled_shouldThrow() {
            Event event = buildEvent(1L, "Show", Event.EventStatus.CANCELLED, 100, 100);
            given(eventRepository.findById(1L)).willReturn(Optional.of(event));

            assertThatThrownBy(() -> eventService.updateEvent(1L, "New Name", null, null, null, null, 0))
                    .isInstanceOf(EventCancelledException.class);
        }
    }

    // ---- Helpers ----
    private Event buildEvent(Long id, String name, Event.EventStatus status, int total, int available) {
        return Event.builder()
                .id(id).name(name).status(status)
                .category(Event.EventCategory.CONCERT)
                .location("Montreal").eventDate(futureDate)
                .totalSeats(total).availableSeats(available)
                .price(new BigDecimal("49.99"))
                .organizer(adminUser).build();
    }

    private Reservation buildReservation(Long id, Event event, User user, Reservation.ReservationStatus status) {
        return Reservation.builder()
                .id(id).event(event).user(user)
                .numTickets(2).totalPrice(new BigDecimal("99.98"))
                .confirmationCode("TKT-ABC123").status(status).build();
    }
}
