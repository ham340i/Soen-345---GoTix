package com.ticketreservation.service;

import com.ticketreservation.exception.TicketReservationException.*;
import com.ticketreservation.model.Event;
import com.ticketreservation.model.Reservation;
import com.ticketreservation.model.User;
import com.ticketreservation.repository.EventRepository;
import com.ticketreservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;
    private final NotificationService notificationService;

    /**
     * Admin: Add a new event.
     * Functional Requirement: Administrators should be able to add new events.
     */
    public Event createEvent(String name, String description, Event.EventCategory category,
                              String location, LocalDateTime eventDate, int totalSeats,
                              BigDecimal price, User organizer) {
        log.info("Creating event: name={}, location={}, date={}", name, location, eventDate);

        if (eventDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Event date must be in the future");
        }
        if (totalSeats <= 0) {
            throw new IllegalArgumentException("Total seats must be positive");
        }

        Event event = Event.builder()
                .name(name)
                .description(description)
                .category(category)
                .location(location)
                .eventDate(eventDate)
                .totalSeats(totalSeats)
                .availableSeats(totalSeats)
                .price(price)
                .organizer(organizer)
                .status(Event.EventStatus.ACTIVE)
                .build();

        Event saved = eventRepository.save(event);
        log.info("Event created with id={}", saved.getId());
        return saved;
    }

    /**
     * Admin: Edit an existing event.
     * Functional Requirement: Administrators should be able to edit existing events.
     */
    public Event updateEvent(Long eventId, String name, String description,
                              String location, LocalDateTime eventDate,
                              BigDecimal price, int totalSeats) {
        Event event = findById(eventId);

        if (event.getStatus() == Event.EventStatus.CANCELLED) {
            throw new EventCancelledException(event.getName());
        }

        if (name != null && !name.isBlank()) event.setName(name);
        if (description != null) event.setDescription(description);
        if (location != null && !location.isBlank()) event.setLocation(location);
        if (eventDate != null) {
            if (eventDate.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Event date must be in the future");
            }
            event.setEventDate(eventDate);
        }
        if (price != null && price.compareTo(BigDecimal.ZERO) >= 0) event.setPrice(price);
        if (totalSeats > 0) {
            int usedSeats = event.getTotalSeats() - event.getAvailableSeats();
            if (totalSeats < usedSeats) {
                throw new IllegalArgumentException("Cannot reduce seats below already reserved count: " + usedSeats);
            }
            event.setTotalSeats(totalSeats);
            event.setAvailableSeats(totalSeats - usedSeats);
        }

        Event updated = eventRepository.save(event);
        log.info("Event {} updated", eventId);
        return updated;
    }

    /**
     * Admin: Cancel an event.
     * Functional Requirement: Administrators should be able to cancel events.
     */
    public Event cancelEvent(Long eventId) {
        Event event = findById(eventId);

        if (event.getStatus() == Event.EventStatus.CANCELLED) {
            throw new EventCancelledException(event.getName());
        }

        event.setStatus(Event.EventStatus.CANCELLED);
        Event cancelled = eventRepository.save(event);

        // Notify all confirmed reservation holders
        List<Reservation> reservations = reservationRepository.findByEventIdOrderByReservedAtDesc(eventId);
        reservations.stream()
                .filter(r -> r.getStatus() == Reservation.ReservationStatus.CONFIRMED)
                .forEach(reservation -> {
                    reservation.cancel("Event cancelled by organizer");
                    reservationRepository.save(reservation);
                    try {
                        notificationService.sendCancellationNotification(reservation);
                    } catch (Exception e) {
                        log.warn("Failed to notify user {} about event cancellation", reservation.getUser().getId());
                    }
                });

        log.info("Event {} cancelled. {} reservations cancelled.", eventId, reservations.size());
        return cancelled;
    }

    /**
     * Customer: View list of available events.
     */
    @Transactional(readOnly = true)
    public List<Event> getAvailableEvents() {
        return eventRepository.findByStatusOrderByEventDateAsc(Event.EventStatus.ACTIVE);
    }

    /**
     * Customer: Search and filter events by date, location, or category.
     */
    @Transactional(readOnly = true)
    public List<Event> filterEvents(Event.EventCategory category, String location,
                                     LocalDateTime startDate, LocalDateTime endDate) {
        return eventRepository.filterEvents(category, location, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<Event> searchEvents(String keyword) {
        return eventRepository.searchEvents(keyword);
    }

    @Transactional(readOnly = true)
    public Event findById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));
    }

    @Transactional(readOnly = true)
    public List<Event> getEventsByOrganizer(Long organizerId) {
        return eventRepository.findByOrganizerIdOrderByCreatedAtDesc(organizerId);
    }
}
