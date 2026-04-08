package com.ticketreservation.dto;

import com.ticketreservation.model.Event;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

    private Long id;
    private String name;
    private String description;
    private Event.EventCategory category;
    private String location;
    private LocalDateTime eventDate;
    private int totalSeats;
    private int availableSeats;
    private BigDecimal price;
    private Event.EventStatus status;
    private LocalDateTime createdAt;
    private String organizerName;

    public static EventResponse from(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .category(event.getCategory())
                .location(event.getLocation())
                .eventDate(event.getEventDate())
                .totalSeats(event.getTotalSeats())
                .availableSeats(event.getAvailableSeats())
                .price(event.getPrice())
                .status(event.getStatus())
                .createdAt(event.getCreatedAt())
                .organizerName(event.getOrganizer() != null ? event.getOrganizer().getName() : null)
                .build();
    }
}
