package com.ticketreservation.service;

import com.ticketreservation.model.Reservation;
import com.ticketreservation.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Notification Service: Handles email and SMS confirmations.
 * Non-Functional Requirement: Users receive confirmations via email or SMS.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;

    /**
     * Send a reservation confirmation email/SMS to the user.
     */
    public void sendReservationConfirmation(Reservation reservation) {
        User user = reservation.getUser();
        String subject = "Ticket Reservation Confirmed - " + reservation.getConfirmationCode();
        String body = buildConfirmationMessage(reservation);

        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            sendEmail(user.getEmail(), subject, body);
        }
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isBlank()) {
            sendSms(user.getPhoneNumber(), "Your ticket is confirmed! Code: " + reservation.getConfirmationCode()
                    + " for " + reservation.getEvent().getName());
        }

        log.info("Confirmation sent for reservation {}", reservation.getConfirmationCode());
    }

    /**
     * Send a cancellation notification.
     */
    public void sendCancellationNotification(Reservation reservation) {
        User user = reservation.getUser();
        String subject = "Reservation Cancelled - " + reservation.getConfirmationCode();
        String body = "Dear " + user.getName() + ",\n\n"
                + "Your reservation " + reservation.getConfirmationCode()
                + " for " + reservation.getEvent().getName() + " has been cancelled.\n"
                + "Reason: " + reservation.getCancellationReason() + "\n\n"
                + "If you have any questions, please contact support.\n\nTicketReservation Team";

        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            sendEmail(user.getEmail(), subject, body);
        }
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isBlank()) {
            sendSms(user.getPhoneNumber(), "Reservation " + reservation.getConfirmationCode() + " cancelled for "
                    + reservation.getEvent().getName());
        }

        log.info("Cancellation notification sent for reservation {}", reservation.getConfirmationCode());
    }

    /**
     * Send a welcome email to new users.
     */
    public void sendWelcomeNotification(User user) {
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            String subject = "Welcome to GoTix!";
            String body = "Dear " + user.getName() + ",\n\nWelcome to GoTix! "
                    + "Browse events, book tickets, and enjoy the show!\n\nThe GoTix Team";
            sendEmail(user.getEmail(), subject, body);
        }
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("noreply@ticketreservation.com");
            mailSender.send(message);
            log.debug("Email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw e;
        }
    }

    /**
     * SMS sending - integrates with SMS provider (Twilio, etc.)
     * For now, logs the message (extensible for real SMS integration).
     */
    private void sendSms(String phoneNumber, String message) {
        // Placeholder: In production, integrate with Twilio or AWS SNS
        log.info("SMS to {}: {}", phoneNumber, message);
    }

    private String buildConfirmationMessage(Reservation reservation) {
        return String.format(
                "Dear %s,\n\nYour reservation is CONFIRMED!\n\n"
                        + "Confirmation Code: %s\n"
                        + "Event: %s\n"
                        + "Date: %s\n"
                        + "Location: %s\n"
                        + "Tickets: %d\n"
                        + "Total Price: $%.2f\n\n"
                        + "Enjoy the event!\nTicketReservation Team",
                reservation.getUser().getName(),
                reservation.getConfirmationCode(),
                reservation.getEvent().getName(),
                reservation.getEvent().getEventDate(),
                reservation.getEvent().getLocation(),
                reservation.getNumTickets(),
                reservation.getTotalPrice()
        );
    }
}
