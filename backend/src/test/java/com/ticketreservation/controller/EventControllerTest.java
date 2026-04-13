package com.ticketreservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketreservation.config.JwtUtil;
import com.ticketreservation.dto.EventRequest;
import com.ticketreservation.exception.TicketReservationException.ResourceNotFoundException;
import com.ticketreservation.model.Event;
import com.ticketreservation.model.User;
import com.ticketreservation.service.EventService;
import com.ticketreservation.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller-layer tests for EventController.
 * Covers public read endpoints and admin write endpoints.
 */
@WebMvcTest(EventController.class)
@DisplayName("EventController Tests")
class EventControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private EventService eventService;
    @MockBean private UserService userService;
    @MockBean private JwtUtil jwtUtil;

    private Event sampleEvent;
    private User adminUser;

    @BeforeEach
    void setUp() {
        adminUser = User.builder().id(1L).name("Admin").email("admin@test.com")
                .role(User.Role.ADMIN).password("encoded").active(true).build();

        sampleEvent = Event.builder()
                .id(1L).name("Jazz Festival").description("Great jazz music")
                .category(Event.EventCategory.CONCERT).location("Montreal Bell Centre")
                .eventDate(LocalDateTime.now().plusDays(30))
                .totalSeats(500).availableSeats(500)
                .price(new BigDecimal("49.99"))
                .status(Event.EventStatus.ACTIVE)
                .organizer(adminUser)
                .build();
    }

    // ── Public GET endpoints ──────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/events")
    class GetAllEventsTests {

        @Test
        @DisplayName("Returns 200 with list of active events — no auth required")
        void getAllEvents_returns200() throws Exception {
            given(eventService.getAvailableEvents()).willReturn(List.of(sampleEvent));

            mockMvc.perform(get("/api/events"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].name").value("Jazz Festival"))
                    .andExpect(jsonPath("$[0].availableSeats").value(500));
        }

        @Test
        @DisplayName("Returns empty array when no events available")
        void getAllEvents_empty_returns200() throws Exception {
            given(eventService.getAvailableEvents()).willReturn(List.of());

            mockMvc.perform(get("/api/events"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/events/{id}")
    class GetEventByIdTests {

        @Test
        @DisplayName("Returns 200 with event data for valid ID")
        void getEvent_valid_returns200() throws Exception {
            given(eventService.findById(1L)).willReturn(sampleEvent);

            mockMvc.perform(get("/api/events/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.category").value("CONCERT"))
                    .andExpect(jsonPath("$.price").value(49.99));
        }

        @Test
        @DisplayName("Returns 404 for unknown event ID")
        void getEvent_notFound_returns404() throws Exception {
            given(eventService.findById(99L))
                    .willThrow(new ResourceNotFoundException("Event", 99L));

            mockMvc.perform(get("/api/events/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    @Nested
    @DisplayName("GET /api/events/search")
    class SearchEventsTests {

        @Test
        @DisplayName("Returns matching events for keyword")
        void searchEvents_returnsMatches() throws Exception {
            given(eventService.searchEvents("jazz")).willReturn(List.of(sampleEvent));

            mockMvc.perform(get("/api/events/search").param("keyword", "jazz"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("Jazz Festival"));
        }

        @Test
        @DisplayName("Returns empty array for no matches")
        void searchEvents_noMatches_returnsEmpty() throws Exception {
            given(eventService.searchEvents("opera")).willReturn(List.of());

            mockMvc.perform(get("/api/events/search").param("keyword", "opera"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/events/filter")
    class FilterEventsTests {

        @Test
        @DisplayName("Filters by category and location")
        void filterEvents_byCategoryAndLocation() throws Exception {
            given(eventService.filterEvents(Event.EventCategory.CONCERT, "Montreal", null, null))
                    .willReturn(List.of(sampleEvent));

            mockMvc.perform(get("/api/events/filter")
                            .param("category", "CONCERT")
                            .param("location", "Montreal"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].location").value("Montreal Bell Centre"));
        }

        @Test
        @DisplayName("Returns all active events when no filters provided")
        void filterEvents_noParams_returnsAll() throws Exception {
            given(eventService.filterEvents(null, null, null, null))
                    .willReturn(List.of(sampleEvent));

            mockMvc.perform(get("/api/events/filter"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    // ── Admin write endpoints ─────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/events — Admin Create")
    class CreateEventTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Admin creates event — returns 201")
        void createEvent_asAdmin_returns201() throws Exception {
            given(jwtUtil.extractUserId(any())).willReturn(1L);
            given(userService.findById(1L)).willReturn(adminUser);
            given(eventService.createEvent(any(), any(), any(), any(), any(), anyInt(), any(), any()))
                    .willReturn(sampleEvent);

            EventRequest req = EventRequest.builder()
                    .name("Jazz Festival").description("Great jazz")
                    .category(Event.EventCategory.CONCERT)
                    .location("Montreal Bell Centre")
                    .eventDate(LocalDateTime.now().plusDays(30))
                    .totalSeats(500).price(new BigDecimal("49.99")).build();

            mockMvc.perform(post("/api/events")
                            .with(csrf())
                            .header("Authorization", "Bearer " + "testtoken")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Jazz Festival"));
        }

        @Test
        @WithMockUser(roles = "CUSTOMER")
        @DisplayName("Customer cannot create event — returns 403")
        void createEvent_asCustomer_returns403() throws Exception {
            EventRequest req = EventRequest.builder()
                    .name("Jazz Festival").category(Event.EventCategory.CONCERT)
                    .location("Montreal").eventDate(LocalDateTime.now().plusDays(30))
                    .totalSeats(100).price(new BigDecimal("29.99")).build();

            mockMvc.perform(post("/api/events")
                            .with(csrf())
                            .header("Authorization", "Bearer testtoken")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Create event with missing name returns 400")
        void createEvent_missingName_returns400() throws Exception {
            EventRequest req = EventRequest.builder()
                    .category(Event.EventCategory.CONCERT)
                    .location("Montreal").eventDate(LocalDateTime.now().plusDays(30))
                    .totalSeats(100).price(new BigDecimal("29.99")).build();

            mockMvc.perform(post("/api/events")
                            .with(csrf())
                            .header("Authorization", "Bearer testtoken")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/events/{id} — Admin Cancel")
    class CancelEventTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Admin cancels event — returns 200 with CANCELLED status")
        void cancelEvent_asAdmin_returns200() throws Exception {
            Event cancelled = Event.builder()
                    .id(1L).name("Jazz Festival").category(Event.EventCategory.CONCERT)
                    .location("Montreal Bell Centre")
                    .eventDate(LocalDateTime.now().plusDays(30))
                    .totalSeats(500).availableSeats(500)
                    .price(new BigDecimal("49.99"))
                    .status(Event.EventStatus.CANCELLED)
                    .organizer(adminUser).build();

            given(eventService.cancelEvent(1L)).willReturn(cancelled);

            mockMvc.perform(delete("/api/events/1")
                            .with(csrf())
                            .header("Authorization", "Bearer testtoken"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CANCELLED"));
        }

        @Test
        @WithMockUser(roles = "CUSTOMER")
        @DisplayName("Customer cannot cancel event — returns 403")
        void cancelEvent_asCustomer_returns403() throws Exception {
            mockMvc.perform(delete("/api/events/1")
                            .with(csrf())
                            .header("Authorization", "Bearer testtoken"))
                    .andExpect(status().isForbidden());
        }
    }
}
