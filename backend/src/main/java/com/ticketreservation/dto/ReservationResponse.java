package com.ticketreservation.dto;

import com.ticketreservation.model.Reservation;
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
public class ReservationResponse {

    private Long id;
    private String confirmationCode;
    private Long eventId;
    private String eventName;
    private String eventLocation;
    private LocalDateTime eventDate;
    private int numTickets;
    private BigDecimal totalPrice;
    private Reservation.ReservationStatus status;
    private LocalDateTime reservedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;

    public static ReservationResponse from(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .confirmationCode(reservation.getConfirmationCode())
                .eventId(reservation.getEvent().getId())
                .eventName(reservation.getEvent().getName())
                .eventLocation(reservation.getEvent().getLocation())
                .eventDate(reservation.getEvent().getEventDate())
                .numTickets(reservation.getNumTickets())
                .totalPrice(reservation.getTotalPrice())
                .status(reservation.getStatus())
                .reservedAt(reservation.getReservedAt())
                .cancelledAt(reservation.getCancelledAt())
                .cancellationReason(reservation.getCancellationReason())
                .build();
    }
}
