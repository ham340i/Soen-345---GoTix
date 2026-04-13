package com.ticketreservation.service;

import com.ticketreservation.model.Reservation;
import com.ticketreservation.model.User;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Notification Service: Handles email and SMS confirmations.
 * Non-Functional Requirement: Users receive confirmations via email or SMS.
 *
 * Email: Spring Boot Mail (SMTP/Gmail)
 * SMS:   Twilio — set TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, TWILIO_FROM_NUMBER
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;

    @Value("${twilio.account-sid:}")
    private String twilioAccountSid;

    @Value("${twilio.auth-token:}")
    private String twilioAuthToken;

    @Value("${twilio.from-number:}")
    private String twilioFromNumber;

    @Value("${spring.mail.username:noreply@gotix.com}")
    private String fromEmail;

    @Value("${app.mail.from:noreply@gotix.local}")
    private String fallbackFromEmail;

    /** Initialize Twilio SDK once credentials are available. */
    @PostConstruct
    private void initTwilio() {
        if (twilioAccountSid != null && !twilioAccountSid.isBlank()
                && twilioAuthToken != null && !twilioAuthToken.isBlank()) {
            Twilio.init(twilioAccountSid, twilioAuthToken);
            log.info("Twilio SMS initialized successfully");
        } else {
            log.warn("Twilio credentials not configured — SMS will be logged only. "
                    + "Set TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, TWILIO_FROM_NUMBER to enable real SMS.");
        }
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /** Send reservation confirmation via the user's preferred channel(s). */
    public void sendReservationConfirmation(Reservation reservation) {
        User user = reservation.getUser();
        User.NotificationPreference pref = getEffectivePref(user);

        String emailSubject = "Ticket Reservation Confirmed — " + reservation.getConfirmationCode();
        String emailBody    = buildConfirmationEmail(reservation);
        String smsBody      = buildConfirmationSms(reservation);

        dispatch(user, pref, emailSubject, emailBody, smsBody);
        log.info("Confirmation sent for reservation {}", reservation.getConfirmationCode());
    }

    /** Send cancellation notification via the user's preferred channel(s). */
    public void sendCancellationNotification(Reservation reservation) {
        User user = reservation.getUser();
        User.NotificationPreference pref = getEffectivePref(user);

        String emailSubject = "Reservation Cancelled — " + reservation.getConfirmationCode();
        String emailBody    = buildCancellationEmail(reservation);
        String smsBody      = "GoTix: Reservation " + reservation.getConfirmationCode()
                + " for " + reservation.getEvent().getName() + " has been cancelled.";

        dispatch(user, pref, emailSubject, emailBody, smsBody);
        log.info("Cancellation notification sent for reservation {}", reservation.getConfirmationCode());
    }

    /** Send a welcome message to newly registered users. */
    public void sendWelcomeNotification(User user) {
        User.NotificationPreference pref = getEffectivePref(user);

        String emailSubject = "Welcome to GoTix!";
        String emailBody    = "Dear " + user.getName() + ",\n\n"
                + "Welcome to GoTix! Browse events, book tickets, and enjoy the show!\n\n"
                + "The GoTix Team";
        String smsBody      = "Welcome to GoTix, " + user.getName()
                + "! Browse events and book tickets at gotix.com";

        dispatch(user, pref, emailSubject, emailBody, smsBody);
    }

    // ── Internal dispatch ─────────────────────────────────────────────────────

    private void dispatch(User user, User.NotificationPreference pref,
                          String emailSubject, String emailBody, String smsBody) {
        boolean hasEmail = user.getEmail() != null && !user.getEmail().isBlank();
        boolean hasPhone = user.getPhoneNumber() != null && !user.getPhoneNumber().isBlank();

        switch (pref) {
            case EMAIL -> {
                if (hasEmail) sendEmail(user.getEmail(), emailSubject, emailBody);
                else          log.warn("User {} prefers EMAIL but has no email address", user.getId());
            }
            case SMS -> {
                if (hasPhone) sendSms(user.getPhoneNumber(), smsBody);
                else          log.warn("User {} prefers SMS but has no phone number", user.getId());
            }
            case BOTH -> {
                if (hasEmail) sendEmail(user.getEmail(), emailSubject, emailBody);
                if (hasPhone) sendSms(user.getPhoneNumber(), smsBody);
            }
        }
    }

    private User.NotificationPreference getEffectivePref(User user) {
        boolean hasEmail = user.getEmail() != null && !user.getEmail().isBlank();
        boolean hasPhone = user.getPhoneNumber() != null && !user.getPhoneNumber().isBlank();
        User.NotificationPreference pref = user.getNotificationPreference() != null
                ? user.getNotificationPreference()
                : User.NotificationPreference.EMAIL;

        if (pref == User.NotificationPreference.EMAIL && !hasEmail && hasPhone) {
            return User.NotificationPreference.SMS;
        }
        if (pref == User.NotificationPreference.SMS && !hasPhone && hasEmail) {
            return User.NotificationPreference.EMAIL;
        }
        return pref;
    }

    // ── Channel implementations ───────────────────────────────────────────────

    private void sendEmail(String to, String subject, String body) {
        try {
            String sender = (fromEmail != null && !fromEmail.isBlank())
                    ? fromEmail
                    : fallbackFromEmail;
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom(sender);
            mailSender.send(message);
            log.info("Email sent to {} from {}: {}", to, sender, subject);
        } catch (Exception e) {
            // Graceful fallback: log the email content instead of failing
            log.warn("Failed to send email to {} (SMTP unavailable: {}). Email content logged below:",
                    to, e.getMessage());
            log.warn("TO: {}", to);
            log.warn("SUBJECT: {}", subject);
            log.warn("BODY:\n{}", body);
        }
    }

    private void sendSms(String phoneNumber, String body) {
        if (twilioAccountSid == null || twilioAccountSid.isBlank()
                || twilioFromNumber == null || twilioFromNumber.isBlank()) {
            log.info("[SMS-STUB] To={} | Message={}", phoneNumber, body);
            return;
        }
        try {
            Message.creator(new PhoneNumber(phoneNumber), new PhoneNumber(twilioFromNumber), body)
                    .create();
            log.debug("SMS sent to {}", phoneNumber);
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", phoneNumber, e.getMessage());
            throw e;
        }
    }

    // ── Message builders ──────────────────────────────────────────────────────

    private String buildConfirmationEmail(Reservation reservation) {
        return String.format(
                "Dear %s,%n%n"
                        + "Your reservation is CONFIRMED! \uD83C\uDF89%n%n"
                        + "──────────────────────────────%n"
                        + "Confirmation Code : %s%n"
                        + "Event             : %s%n"
                        + "Date              : %s%n"
                        + "Location          : %s%n"
                        + "Tickets           : %d%n"
                        + "Total Price       : $%.2f%n"
                        + "──────────────────────────────%n%n"
                        + "Show this code at the entrance. Enjoy the event!%n%n"
                        + "The GoTix Team",
                reservation.getUser().getName(),
                reservation.getConfirmationCode(),
                reservation.getEvent().getName(),
                reservation.getEvent().getEventDate(),
                reservation.getEvent().getLocation(),
                reservation.getNumTickets(),
                reservation.getTotalPrice()
        );
    }

    private String buildConfirmationSms(Reservation reservation) {
        return String.format(
                "GoTix CONFIRMED! Code: %s | %s on %s | %d ticket(s) | $%.2f",
                reservation.getConfirmationCode(),
                reservation.getEvent().getName(),
                reservation.getEvent().getEventDate(),
                reservation.getNumTickets(),
                reservation.getTotalPrice()
        );
    }

    private String buildCancellationEmail(Reservation reservation) {
        return String.format(
                "Dear %s,%n%n"
                        + "Your reservation has been CANCELLED.%n%n"
                        + "Confirmation Code : %s%n"
                        + "Event             : %s%n"
                        + "Reason            : %s%n%n"
                        + "If this was a mistake or you have questions, please contact support.%n%n"
                        + "The GoTix Team",
                reservation.getUser().getName(),
                reservation.getConfirmationCode(),
                reservation.getEvent().getName(),
                reservation.getCancellationReason()
        );
    }
}
