package com.example.gotix.service;

import com.example.gotix.model.Event;
import com.example.gotix.model.Reservation;
import com.example.gotix.store.EventStore;
import com.example.gotix.store.ReservationStore;

import java.util.List;
import java.util.UUID;

public class ReservationService {

    /**
     * Reserve tickets for an event.
     * Returns the created Reservation, or null if seats are unavailable.
     */
    public Reservation reserve(String userId, String eventId, int numTickets) {
        if (numTickets <= 0) return null;
        Event event = EventStore.findById(eventId);
        if (event == null) return null;

        boolean seated = EventStore.reserveSeats(eventId, numTickets);
        if (!seated) return null;

        Reservation reservation = new Reservation(
                UUID.randomUUID().toString(),
                eventId,
                userId,
                event.getTitle(),
                numTickets
        );
        return ReservationStore.add(reservation);
    }

    /**
     * Cancel an existing reservation by ID.
     * Returns true on success, false if not found or already cancelled.
     */
    public boolean cancel(String reservationId) {
        return ReservationStore.cancel(reservationId);
    }

    /** Get all reservations for a given user. */
    public List<Reservation> getByUser(String userId) {
        return ReservationStore.getByUser(userId);
    }
}
