package com.ticketreservation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class TicketReservationException extends RuntimeException {

    public TicketReservationException(String message) {
        super(message);
    }

    public TicketReservationException(String message, Throwable cause) {
        super(message, cause);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class ResourceNotFoundException extends TicketReservationException {
        public ResourceNotFoundException(String resource, Long id) {
            super(resource + " not found with id: " + id);
        }
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class InsufficientSeatsException extends TicketReservationException {
        public InsufficientSeatsException(String eventName, int requested, int available) {
            super("Not enough seats for '" + eventName + "'. Requested: " + requested + ", Available: " + available);
        }
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class UserAlreadyExistsException extends TicketReservationException {
        public UserAlreadyExistsException(String field, String value) {
            super("User already exists with " + field + ": " + value);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class EventCancelledException extends TicketReservationException {
        public EventCancelledException(String eventName) {
            super("Cannot reserve tickets for cancelled event: " + eventName);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class ReservationAlreadyCancelledException extends TicketReservationException {
        public ReservationAlreadyCancelledException(String confirmationCode) {
            super("Reservation " + confirmationCode + " is already cancelled");
        }
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    public static class UnauthorizedAccessException extends TicketReservationException {
        public UnauthorizedAccessException(String message) {
            super(message);
        }
    }
}
