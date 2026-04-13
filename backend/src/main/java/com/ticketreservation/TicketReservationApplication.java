package com.ticketreservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

/**
 * TicketReservationApplication - Main entry point.
 *
 * SOEN 345: Software Testing, Verification and Quality Assurance
 * Cloud-based Ticket Reservation System
 *
 * Features:
 * - User registration (email or phone)
 * - Event browsing, searching, and filtering
 * - Ticket reservation and cancellation
 * - Email/SMS confirmation notifications
 * - Admin event management (add, edit, cancel)
 * - CI/CD with GitHub Actions
 * - JUnit 5 test suite with JaCoCo coverage
 * - Concurrent booking protection via pessimistic locking + optimistic locking + Spring Retry
 */
@SpringBootApplication
@EnableRetry
public class TicketReservationApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketReservationApplication.class, args);
    }
}
