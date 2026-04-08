package com.example.gotix.store;

import com.example.gotix.model.Event;
import java.util.ArrayList;
import java.util.List;

public class EventStore {
    private static List<Event> events = new ArrayList<>();

    static {
        events.add(new Event("1", "Inception", "2023-12-01", "Montreal", "Movie"));
        events.add(new Event("2", "Coldplay Live", "2023-12-15", "Toronto", "Concert"));
        events.add(new Event("3", "NBA Finals", "2024-06-10", "New York", "Sports"));
        events.add(new Event("4", "Paris Flight", "2023-11-20", "Paris", "Travel"));
        events.add(new Event("5", "Interstellar", "2023-12-05", "Vancouver", "Movie"));
    }

    public static List<Event> getAll() {
        return events;
    }
}
