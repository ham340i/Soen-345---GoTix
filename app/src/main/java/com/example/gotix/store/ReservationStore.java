package com.example.gotix.store;

import com.example.gotix.model.Event;
import com.example.gotix.model.Reservation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReservationStore {
    private static final List<Reservation> reservations = new ArrayList<>();

    public static synchronized Reservation add(Reservation reservation) {
        reservations.add(reservation);
        return reservation;
    }

    public static synchronized boolean cancel(String reservationId) {
        for (Reservation r : reservations) {
            if (r.getId().equals(reservationId)) {
                if ("CANCELLED".equals(r.getStatus())) return false;
                r.setStatus("CANCELLED");
                // Release the seats back to the event
                EventStore.releaseSeats(r.getEventId(), r.getNumTickets());
                return true;
            }
        }
        return false;
    }

    public static List<Reservation> getByUser(String userId) {
        return reservations.stream()
                .filter(r -> r.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    public static List<Reservation> getAll() {
        return new ArrayList<>(reservations);
    }

    public static Reservation findById(String id) {
        return reservations.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
