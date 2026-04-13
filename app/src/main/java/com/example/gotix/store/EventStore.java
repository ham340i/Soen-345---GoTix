package com.example.gotix.store;

import com.example.gotix.model.Event;
import java.util.ArrayList;
import java.util.List;

public class EventStore {
    private static final List<Event> events = new ArrayList<>();

    static {
        events.add(new Event("1", "Inception",     "2025-12-01", "Montreal", "Movie",   50));
        events.add(new Event("2", "Coldplay Live", "2025-12-15", "Toronto",  "Concert", 200));
        events.add(new Event("3", "NBA Finals",    "2026-06-10", "New York", "Sports",  100));
        events.add(new Event("4", "Paris Flight",  "2025-11-20", "Paris",    "Travel",  30));
        events.add(new Event("5", "Interstellar",  "2025-12-05", "Vancouver","Movie",   80));
    }

    public static List<Event> getAll() {
        return new ArrayList<>(events);
    }

    public static Event findById(String id) {
        return events.stream().filter(e -> e.getId().equals(id)).findFirst().orElse(null);
    }

    /** Reserve seats — returns false if not enough seats available. */
    public static synchronized boolean reserveSeats(String eventId, int count) {
        Event event = findById(eventId);
        if (event == null || event.getAvailableSeats() < count) return false;
        event.setAvailableSeats(event.getAvailableSeats() - count);
        return true;
    }

    /** Release seats back to the pool when a reservation is cancelled. */
    public static synchronized void releaseSeats(String eventId, int count) {
        Event event = findById(eventId);
        if (event != null) {
            event.setAvailableSeats(event.getAvailableSeats() + count);
        }
    }
}
