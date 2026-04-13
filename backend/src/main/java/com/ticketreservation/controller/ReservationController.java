package com.ticketreservation.controller;

import com.ticketreservation.config.JwtUtil;
import com.ticketreservation.dto.ReservationRequest;
import com.ticketreservation.dto.ReservationResponse;
import com.ticketreservation.model.Reservation;
import com.ticketreservation.model.User;
import com.ticketreservation.service.ReservationService;
import com.ticketreservation.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Reservation REST API — all endpoints require authentication.
 *
 * POST   /api/reservations                     — reserve tickets           (FR-04)
 * GET    /api/reservations/my                  — view my reservations
 * GET    /api/reservations/{id}                — get reservation by ID
 * GET    /api/reservations/code/{code}         — lookup by confirmation code
 * DELETE /api/reservations/{id}                — cancel reservation        (FR-05)
 * GET    /api/reservations/event/{eventId}     — ADMIN: all reservations for event
 */
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Slf4j
public class ReservationController {

    private final ReservationService reservationService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * FR-04: Reserve tickets for an event.
     * Atomically decrements available seats and generates a confirmation code.
     * Sends email/SMS confirmation (FR-06).
     */
    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationRequest request,
            @RequestHeader("Authorization") String authHeader) {

        User user = getCurrentUser(authHeader);
        Reservation reservation = reservationService.createReservation(
                user, request.getEventId(), request.getNumTickets());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ReservationResponse.from(reservation));
    }

    /**
     * Get all reservations belonging to the currently authenticated user.
     */
    @GetMapping("/my")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(
            @RequestHeader("Authorization") String authHeader) {

        Long userId = extractUserIdFromHeader(authHeader);
        List<ReservationResponse> reservations = reservationService.getUserReservations(userId).stream()
                .map(ReservationResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(reservations);
    }

    /**
     * Get a single reservation by its database ID.
     * Users can only see their own; admins can see any.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getReservation(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        Reservation reservation = reservationService.findById(id);
        User requester = getCurrentUser(authHeader);

        // Customers may only view their own reservations
        boolean isOwner = reservation.getUser().getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == User.Role.ADMIN;
        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(ReservationResponse.from(reservation));
    }

    /**
     * Lookup reservation by confirmation code (e.g., TKT-ABCD1234).
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<ReservationResponse> getByConfirmationCode(
            @PathVariable String code,
            @RequestHeader("Authorization") String authHeader) {

        Reservation reservation = reservationService.findByConfirmationCode(code);
        User requester = getCurrentUser(authHeader);

        boolean isOwner = reservation.getUser().getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == User.Role.ADMIN;
        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(ReservationResponse.from(reservation));
    }

    /**
     * FR-05: Cancel a reservation.
     * Releases seats back to the event pool and sends a cancellation notification (FR-06).
     * Customers can cancel their own; admins can cancel any.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ReservationResponse> cancelReservation(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        User requester = getCurrentUser(authHeader);
        Reservation cancelled = reservationService.cancelReservation(id, requester);
        return ResponseEntity.ok(ReservationResponse.from(cancelled));
    }

    /**
     * ADMIN: View all reservations for a specific event.
     */
    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReservationResponse>> getEventReservations(
            @PathVariable Long eventId) {

        List<ReservationResponse> reservations = reservationService.getEventReservations(eventId).stream()
                .map(ReservationResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(reservations);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User getCurrentUser(String authHeader) {
        Long userId = extractUserIdFromHeader(authHeader);
        return userService.findById(userId);
    }

    private Long extractUserIdFromHeader(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.extractUserId(token);
    }
}
