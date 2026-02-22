package com.example.gotix.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.example.gotix.model.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

public class EventServiceTest {
    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventService();
    }

    @Test
    void testSearchByCategory() {
        List<Event> results = eventService.searchEvents(null, null, "Movie");
        assertEquals(2, results.size());
    }

    @Test
    void testSearchByLocation() {
        List<Event> results = eventService.searchEvents(null, "Montreal", null);
        assertEquals(1, results.size());
        assertEquals("Inception", results.get(0).getTitle());
    }

    @Test
    void testSearchByDate() {
        List<Event> results = eventService.searchEvents("2023-12-15", null, null);
        assertEquals(1, results.size());
        assertEquals("Coldplay Live", results.get(0).getTitle());
    }

    @Test
    void testSearchNoFilters() {
        List<Event> results = eventService.searchEvents("", "", "");
        assertEquals(5, results.size());
    }
}
