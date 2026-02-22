package com.example.gotix.service;

import com.example.gotix.model.Event;
import com.example.gotix.store.EventStore;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EventService {
    public List<Event> getAllEvents() {
        return EventStore.getAll();
    }

    public List<Event> searchEvents(String date, String location, String category) {
        return EventStore.getAll().stream()
                .filter(e -> (date == null || date.isEmpty() || e.getDate().equals(date)))
                .filter(e -> (location == null || location.isEmpty() || e.getLocation().equalsIgnoreCase(location)))
                .filter(e -> (category == null || category.isEmpty() || category.equalsIgnoreCase("All") || e.getCategory().equalsIgnoreCase(category)))
                .collect(Collectors.toList());
    }
}
