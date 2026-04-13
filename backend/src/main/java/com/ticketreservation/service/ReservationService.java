package com.ticketreservation.service;

import com.ticketreservation.exception.TicketReservationException.*;
import com.ticketreservation.model.Event;
import com.ticketreservation.model.Reservation;
import com.ticketreservation.model.User;
import com.ticketreservation.repository.EventRepository;
import com.ticketreservation.repository.ReservationRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final EventRepository eventRepository;
    private final NotificationService notificationService;

    /**
     * Create a reservation for a user.
     *
     * Concurrency strategy (Non-Functional Requirement: support concurrent users):
     *  1. Pessimistic write lock  — SELECT ... FOR UPDATE on the event row serialises
     *     concurrent booking requests at the DB level; no two transactions can hold
     *     the lock simultaneously, so availableSeats is always consistent.
     *  2. Optimistic locking (@Version on Event) — acts as a second safety net for
     *     any code path that reads the event without the explicit lock.
     *  3. @Retryable — if a concurrent transaction wins the lock first and commits,
     *     the loser receives an OptimisticLockException; Spring Retry transparently
     *     retries up to 3 times with exponential back-off before surfacing the error.
     *  4. SERIALIZABLE isolation — guarantees no phantom reads within the transaction.
     */
    @Retryable(
        retryFor  = { ObjectOptimisticLockingFailureException.class, OptimisticLockException.class },
        maxAttempts = 3,
        backoff     = @Backoff(delay = 100, multiplier = 2)
    )
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Reservation createReservation(User user, Long eventId, int numTickets) {
        log.info("Creating reservation: userId={}, eventId={}, numTickets={}", user.getId(), eventId, numTickets);

        if (numTickets <= 0) {
            throw new IllegalArgumentException("Number of tickets must be at least 1");
        }

        // Use the locking query so concurrent requests are serialised at DB level
        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", eventId));

        if (event.getStatus() == Event.EventStatus.CANCELLED) {
            throw new EventCancelledException(event.getName());
        }
        if (event.getStatus() == Event.EventStatus.COMPLETED) {
            throw new IllegalStateException("Event has already completed");
        }
        if (!event.hasAvailableSeats(numTickets)) {
            throw new InsufficientSeatsException(event.getName(), numTickets, event.getAvailableSeats());
        }

        // Decrement seats — protected by the pessimistic lock above
        event.reserveSeats(numTickets);
        eventRepository.save(event);

        BigDecimal totalPrice = event.getPrice().multiply(BigDecimal.valueOf(numTickets));

        Reservation reservation = Reservation.builder()
                .user(user)
                .event(event)
                .numTickets(numTickets)
                .totalPrice(totalPrice)
                .status(Reservation.ReservationStatus.CONFIRMED)
                .build();

        Reservation saved = reservationRepository.save(reservation);
        log.info("Reservation created with confirmationCode={}", saved.getConfirmationCode());

        try {
            notificationService.sendReservationConfirmation(saved);
        } catch (Exception e) {
            log.warn("Failed to send confirmation to user {}: {}", user.getId(), e.getMessage());
        }

        return saved;
    }

    /**
     * Cancel a reservation.
     * Functional Requirement: Users should be able to cancel reservations.
     */
    public Reservation cancelReservation(Long reservationId, User requestingUser) {
        log.info("Cancelling reservation id={} by userId={}", reservationId, requestingUser.getId());

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", reservationId));

        boolean isOwner = reservation.getUser().getId().equals(requestingUser.getId());
        boolean isAdmin = requestingUser.getRole() == User.Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new UnauthorizedAccessException("You are not authorized to cancel this reservation");
        }

        if (reservation.getStatus() == Reservation.ReservationStatus.CANCELLED) {
            throw new ReservationAlreadyCancelledException(reservation.getConfirmationCode());
        }

        // Use the locking query when releasing seats to avoid a race with concurrent bookings
        Event event = eventRepository.findByIdForUpdate(reservation.getEvent().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Event", reservation.getEvent().getId()));
        event.releaseSeats(reservation.getNumTickets());
        eventRepository.save(event);

        reservation.cancel("Cancelled by user request");
        Reservation cancelled = reservationRepository.save(reservation);

        try {
            notificationService.sendCancellationNotification(cancelled);
        } catch (Exception e) {
            log.warn("Failed to send cancellation notification: {}", e.getMessage());
        }

        return cancelled;
    }

    @Transactional(readOnly = true)
    public List<Reservation> getUserReservations(Long userId) {
        return reservationRepository.findByUserIdOrderByReservedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public Reservation findByConfirmationCode(String code) {
        return reservationRepository.findByConfirmationCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with code: " + code));
    }

    @Transactional(readOnly = true)
    public Reservation findById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", id));
    }

    @Transactional(readOnly = true)
    public List<Reservation> getEventReservations(Long eventId) {
        return reservationRepository.findByEventIdOrderByReservedAtDesc(eventId);
    }
}
