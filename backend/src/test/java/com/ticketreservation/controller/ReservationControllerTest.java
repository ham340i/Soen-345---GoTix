package com.ticketreservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketreservation.config.JwtUtil;
import com.ticketreservation.dto.ReservationRequest;
import com.ticketreservation.exception.TicketReservationException.*;
import com.ticketreservation.model.Event;
import com.ticketreservation.model.Reservation;
import com.ticketreservation.model.User;
import com.ticketreservation.service.ReservationService;
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
 * Controller-layer tests for ReservationController.
 */
@WebMvcTest(ReservationController.class)
@DisplayName("ReservationController Tests")
class ReservationControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private ReservationService reservationService;
    @MockBean private UserService userService;
    @MockBean private JwtUtil jwtUtil;

    private User customer;
    private Event event;
    private Reservation confirmedReservation;

    @BeforeEach
    void setUp() {
        customer = User.builder().id(1L).name("Bob").email("bob@test.com")
                .role(User.Role.CUSTOMER).password("encoded").active(true).build();

        event = Event.builder()
                .id(1L).name("Jazz Festival").category(Event.EventCategory.CONCERT)
                .location("Montreal").eventDate(LocalDateTime.now().plusDays(30))
                .totalSeats(200).availableSeats(200)
                .price(new BigDecimal("49.99"))
                .status(Event.EventStatus.ACTIVE).build();

        confirmedReservation = Reservation.builder()
                .id(1L).user(customer).event(event)
                .numTickets(2).totalPrice(new BigDecimal("99.98"))
                .confirmationCode("TKT-ABCD1234")
                .status(Reservation.ReservationStatus.CONFIRMED)
                .reservedAt(LocalDateTime.now()).build();
    }

    // ── Create Reservation ────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/reservations")
    class CreateReservationTests {

        @Test
        @WithMockUser(roles = "CUSTOMER")
        @DisplayName("Valid request returns 201 with confirmation code")
        void createReservation_valid_returns201() throws Exception {
            given(jwtUtil.extractUserId(any())).willReturn(1L);
            given(userService.findById(1L)).willReturn(customer);
            given(reservationService.createReservation(any(), eq(1L), eq(2)))
                    .willReturn(confirmedReservation);

            ReservationRequest req = ReservationRequest.builder()
                    .eventId(1L).numTickets(2).build();

            mockMvc.perform(post("/api/reservations")
                            .with(csrf())
                            .header("Authorization", "Bearer testtoken")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.confirmationCode").value("TKT-ABCD1234"))
                    .andExpect(jsonPath("$.status").value("CONFIRMED"))
                    .andExpect(jsonPath("$.numTickets").value(2))
                    .andExpect(jsonPath("$.totalPrice").value(99.98));
        }

        @Test
        @WithMockUser(roles = "CUSTOMER")
        @DisplayName("Zero tickets returns 400")
        void createReservation_zeroTickets_returns400() throws Exception {
            ReservationRequest req = ReservationRequest.builder()
                    .eventId(1L).numTickets(0).build();

            mockMvc.perform(post("/api/reservations")
                            .with(csrf())
                            .header("Authorization", "Bearer testtoken")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"));
        }

        @Test
        @WithMockUser(roles = "CUSTOMER")
        @DisplayName("Insufficient seats returns 400")
        void createReservation_insufficientSeats_returns400() throws Exception {
            given(jwtUtil.extractUserId(any())).willReturn(1L);
            given(userService.findById(1L)).willReturn(customer);
            given(reservationService.createReservation(any(), any(), anyInt()))
                    .willThrow(new InsufficientSeatsException("Jazz Festival", 50, 5));

            ReservationRequest req = ReservationRequest.builder()
                    .eventId(1L).numTickets(50).build();

            mockMvc.perform(post("/api/reservations")
                            .with(csrf())
                            .header("Authorization", "Bearer testtoken")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Not enough seats")));
        }

        @Test
        @WithMockUser(roles = "CUSTOMER")
        @DisplayName("Cancelled event returns 400")
        void createReservation_cancelledEvent_returns400() throws Exception {
            given(jwtUtil.extractUserId(any())).willReturn(1L);
            given(userService.findById(1L)).willReturn(customer);
            given(reservationService.createReservation(any(), any(), anyInt()))
                    .willThrow(new EventCancelledException("Jazz Festival"));

            ReservationRequest req = ReservationRequest.builder()
                    .eventId(1L).numTickets(2).build();

            mockMvc.perform(post("/api/reservations")
                            .with(csrf())
                            .header("Authorization", "Bearer testtoken")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Unauthenticated request returns 401 or 403")
        void createReservation_noAuth_returnsUnauthorized() throws Exception {
            ReservationRequest req = ReservationRequest.builder()
                    .eventId(1L).numTickets(2).build();

            mockMvc.perform(post("/api/reservations")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(result ->
                            org.junit.jupiter.api.Assertions.assertTrue(
                                    result.getResponse().getStatus() == 401 ||
                                    result.getResponse().getStatus() == 403));
        }
    }

    // ── Get My Reservations ───────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/reservations/my")
    class GetMyReservationsTests {

        @Test
        @WithMockUser(roles = "CUSTOMER")
        @DisplayName("Returns list of own reservations")
        void getMyReservations_returns200() throws Exception {
            given(jwtUtil.extractUserId(any())).willReturn(1L);
            given(reservationService.getUserReservations(1L))
                    .willReturn(List.of(confirmedReservation));

            mockMvc.perform(get("/api/reservations/my")
                            .header("Authorization", "Bearer testtoken"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].confirmationCode").value("TKT-ABCD1234"));
        }

        @Test
        @WithMockUser(roles = "CUSTOMER")
        @DisplayName("Returns empty array when user has no reservations")
        void getMyReservations_empty_returns200() throws Exception {
            given(jwtUtil.extractUserId(any())).willReturn(1L);
            given(reservationService.getUserReservations(1L)).willReturn(List.of());

            mockMvc.perform(get("/api/reservations/my")
                            .header("Authorization", "Bearer testtoken"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    // ── Get By Confirmation Code ──────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/reservations/code/{code}")
    class GetByCodeTests {

        @Test
        @WithMockUser(roles = "CUSTOMER")
        @DisplayName("Owner can look up reservation by confirmation code")
        void getByCode_owner_returns200() throws Exception {
            given(jwtUtil.extractUserId(any())).willReturn(1L);
            given(userService.findById(1L)).willReturn(customer);
            given(reservationService.findByConfirmationCode("TKT-ABCD1234"))
                    .willReturn(confirmedReservation);

            mockMvc.perform(get("/api/reservations/code/TKT-ABCD1234")
                            .header("Authorization", "Bearer testtoken"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.confirmationCode").value("TKT-ABCD1234"))
                    .andExpect(jsonPath("$.eventName").value("Jazz Festival"));
        }

        @Test
        @WithMockUser(roles = "CUSTOMER")
        @DisplayName("Invalid code returns 404")
        void getByCode_invalidCode_returns404() throws Exception {
            given(jwtUtil.extractUserId(any())).willReturn(1L);
            given(userService.findById(1L)).willReturn(customer);
            given(reservationService.findByConfirmationCode("TKT-INVALID"))
                    .willThrow(new ResourceNotFoundException("Reservation not found with code: TKT-INVALID"));

            mockMvc.perform(get("/api/reservations/code/TKT-INVALID")
                            .header("Authorization", "Bearer testtoken"))
                    .andExpect(status().isNotFound());
        }
    }

    // ── Cancel Reservation ────────────────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /api/reservations/{id}")
    class CancelReservationTests {

        @Test
        @WithMockUser(roles = "CUSTOMER")
        @DisplayName("Owner cancels reservation — returns 200 with CANCELLED status")
        void cancelReservation_byOwner_returns200() throws Exception {
            Reservation cancelled = Reservation.builder()
                    .id(1L).user(customer).event(event)
                    .numTickets(2).totalPrice(new BigDecimal("99.98"))
                    .confirmationCode("TKT-ABCD1234")
                    .status(Reservation.ReservationStatus.CANCELLED)
                    .reservedAt(LocalDateTime.now())
                    .cancelledAt(LocalDateTime.now())
                    .cancellationReason("Cancelled by user request").build();

            given(jwtUtil.extractUserId(any())).willReturn(1L);
            given(userService.findById(1L)).willReturn(customer);
            given(reservationService.cancelReservation(1L, customer)).willReturn(cancelled);

            mockMvc.perform(delete("/api/reservations/1")
                            .with(csrf())
                            .header("Authorization", "Bearer testtoken"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CANCELLED"))
                    .andExpect(jsonPath("$.cancellationReason").value("Cancelled by user request"));
        }

        @Test
        @WithMockUser(roles = "CUSTOMER")
        @DisplayName("Already-cancelled reservation returns 400")
        void cancelReservation_alreadyCancelled_returns400() throws Exception {
            given(jwtUtil.extractUserId(any())).willReturn(1L);
            given(userService.findById(1L)).willReturn(customer);
            given(reservationService.cancelReservation(eq(1L), any()))
                    .willThrow(new ReservationAlreadyCancelledException("TKT-ABCD1234"));

            mockMvc.perform(delete("/api/reservations/1")
                            .with(csrf())
                            .header("Authorization", "Bearer testtoken"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "CUSTOMER")
        @DisplayName("Cancelling another user's reservation returns 403")
        void cancelReservation_unauthorized_returns403() throws Exception {
            given(jwtUtil.extractUserId(any())).willReturn(1L);
            given(userService.findById(1L)).willReturn(customer);
            given(reservationService.cancelReservation(eq(1L), any()))
                    .willThrow(new UnauthorizedAccessException("You are not authorized to cancel this reservation"));

            mockMvc.perform(delete("/api/reservations/1")
                            .with(csrf())
                            .header("Authorization", "Bearer testtoken"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "CUSTOMER")
        @DisplayName("Cancelling non-existent reservation returns 404")
        void cancelReservation_notFound_returns404() throws Exception {
            given(jwtUtil.extractUserId(any())).willReturn(1L);
            given(userService.findById(1L)).willReturn(customer);
            given(reservationService.cancelReservation(eq(99L), any()))
                    .willThrow(new ResourceNotFoundException("Reservation", 99L));

            mockMvc.perform(delete("/api/reservations/99")
                            .with(csrf())
                            .header("Authorization", "Bearer testtoken"))
                    .andExpect(status().isNotFound());
        }
    }

    // ── Admin: Get Event Reservations ─────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/reservations/event/{eventId} — Admin")
    class GetEventReservationsTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Admin retrieves all reservations for event")
        void getEventReservations_asAdmin_returns200() throws Exception {
            given(reservationService.getEventReservations(1L))
                    .willReturn(List.of(confirmedReservation));

            mockMvc.perform(get("/api/reservations/event/1")
                            .header("Authorization", "Bearer testtoken"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].eventName").value("Jazz Festival"));
        }

        @Test
        @WithMockUser(roles = "CUSTOMER")
        @DisplayName("Customer cannot access admin endpoint — returns 403")
        void getEventReservations_asCustomer_returns403() throws Exception {
            mockMvc.perform(get("/api/reservations/event/1")
                            .header("Authorization", "Bearer testtoken"))
                    .andExpect(status().isForbidden());
        }
    }
}
