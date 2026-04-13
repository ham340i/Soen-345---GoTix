package com.ticketreservation.controller;

import com.ticketreservation.config.JwtUtil;
import com.ticketreservation.dto.EventRequest;
import com.ticketreservation.dto.EventResponse;
import com.ticketreservation.model.Event;
import com.ticketreservation.model.User;
import com.ticketreservation.service.EventService;
import com.ticketreservation.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Event management REST API.
 *
 * PUBLIC (no auth):
 *   GET  /api/events                           — list all active events         (FR-02)
 *   GET  /api/events/{id}                      — get single event
 *   GET  /api/events/search?keyword=           — search by keyword              (FR-03)
 *   GET  /api/events/filter?category=&location=&startDate=&endDate=  — filter  (FR-03)
 *
 * ADMIN only:
 *   POST   /api/events                         — create event                   (FR-07)
 *   PUT    /api/events/{id}                    — update event                   (FR-08)
 *   DELETE /api/events/{id}                    — cancel event                   (FR-09)
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    // ── Public Endpoints ──────────────────────────────────────────────────────

    /** FR-02: View list of available events. */
    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        List<EventResponse> events = eventService.getAvailableEvents().stream()
                .map(EventResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(events);
    }

    /** Get a single event by ID. */
    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok(EventResponse.from(eventService.findById(id)));
    }

    /** FR-03: Search events by keyword (name, description, location). */
    @GetMapping("/search")
    public ResponseEntity<List<EventResponse>> searchEvents(@RequestParam String keyword) {
        List<EventResponse> results = eventService.searchEvents(keyword).stream()
                .map(EventResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(results);
    }

    /**
     * FR-03: Filter events by date, location, or category.
     * All parameters are optional — any combination is accepted.
     */
    @GetMapping("/filter")
    public ResponseEntity<List<EventResponse>> filterEvents(
            @RequestParam(required = false) Event.EventCategory category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<EventResponse> results = eventService.filterEvents(category, location, startDate, endDate).stream()
                .map(EventResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(results);
    }

    // ── Admin Endpoints ───────────────────────────────────────────────────────

    /** FR-07: Admin adds a new event. */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody EventRequest request,
            @RequestHeader("Authorization") String authHeader) {

        Long adminId = extractUserIdFromHeader(authHeader);
        User admin = userService.findById(adminId);

        Event event = eventService.createEvent(
                request.getName(), request.getDescription(), request.getCategory(),
                request.getLocation(), request.getEventDate(), request.getTotalSeats(),
                request.getPrice(), admin);

        return ResponseEntity.status(HttpStatus.CREATED).body(EventResponse.from(event));
    }

    /** FR-08: Admin edits an existing event. */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long id,
            @RequestBody EventRequest request) {

        Event updated = eventService.updateEvent(
                id, request.getName(), request.getDescription(),
                request.getLocation(), request.getEventDate(),
                request.getPrice(), request.getTotalSeats());

        return ResponseEntity.ok(EventResponse.from(updated));
    }

    /** FR-09: Admin cancels an event. All confirmed reservations are auto-cancelled. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponse> cancelEvent(@PathVariable Long id) {
        Event cancelled = eventService.cancelEvent(id);
        return ResponseEntity.ok(EventResponse.from(cancelled));
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Long extractUserIdFromHeader(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.extractUserId(token);
    }
}
