package com.ticketreservation.config;

import com.ticketreservation.model.Event;
import com.ticketreservation.model.User;
import com.ticketreservation.repository.EventRepository;
import com.ticketreservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository  userRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) { log.info("DB already seeded."); return; }
        log.info("Seeding demo data...");

        User admin = userRepository.save(User.builder()
                .name("Admin User").email("admin@demo.com")
                .password(passwordEncoder.encode("admin1234"))
                .role(User.Role.ADMIN).build());

        userRepository.save(User.builder()
                .name("Alice Smith").email("customer@demo.com")
                .password(passwordEncoder.encode("demo1234"))
                .role(User.Role.CUSTOMER).build());

        eventRepository.save(Event.builder().name("Jazz Festival 2026")
                .description("The biggest jazz festival in Quebec featuring world-class musicians.")
                .category(Event.EventCategory.CONCERT).location("Montreal Bell Centre")
                .eventDate(LocalDateTime.of(2026,6,15,19,0))
                .totalSeats(500).availableSeats(142).price(new BigDecimal("49.99"))
                .organizer(admin).build());

        eventRepository.save(Event.builder().name("Montreal Grand Prix")
                .description("Formula 1 at its finest. Experience the Canadian Grand Prix.")
                .category(Event.EventCategory.SPORTS).location("Circuit Gilles Villeneuve")
                .eventDate(LocalDateTime.of(2026,6,8,14,0))
                .totalSeats(1000).availableSeats(0).price(new BigDecimal("189.99"))
                .organizer(admin).build());

        eventRepository.save(Event.builder().name("Avengers: Secret Wars")
                .description("The most anticipated Marvel film — in IMAX 3D.")
                .category(Event.EventCategory.MOVIE).location("Cineplex Forum Montreal")
                .eventDate(LocalDateTime.of(2026,7,4,20,30))
                .totalSeats(250).availableSeats(87).price(new BigDecimal("18.99"))
                .organizer(admin).build());

        eventRepository.save(Event.builder().name("Tech Summit 2026")
                .description("Three days of AI, cloud, and software engineering talks.")
                .category(Event.EventCategory.CONFERENCE).location("Palais des Congres de Montreal")
                .eventDate(LocalDateTime.of(2026,9,12,9,0))
                .totalSeats(800).availableSeats(310).price(new BigDecimal("299.00"))
                .organizer(admin).build());

        eventRepository.save(Event.builder().name("Cirque du Soleil: Alegria")
                .description("A breathtaking acrobatic spectacle — a timeless show reborn.")
                .category(Event.EventCategory.THEATER).location("Place des Arts, Montreal")
                .eventDate(LocalDateTime.of(2026,8,20,19,30))
                .totalSeats(600).availableSeats(28).price(new BigDecimal("89.99"))
                .organizer(admin).build());

        eventRepository.save(Event.builder().name("Rock Around the Clock Night")
                .description("Classic rock hits from the 70s, 80s, and 90s performed live.")
                .category(Event.EventCategory.CONCERT).location("Bell Centre, Montreal")
                .eventDate(LocalDateTime.of(2026,10,1,20,0))
                .totalSeats(400).availableSeats(400).price(new BigDecimal("65.00"))
                .organizer(admin).build());

        eventRepository.save(Event.builder().name("European River Cruise")
                .description("10-day guided cruise through the Rhine Valley. All-inclusive.")
                .category(Event.EventCategory.TRAVEL).location("Departs Basel, Switzerland")
                .eventDate(LocalDateTime.of(2026,7,20,8,0))
                .totalSeats(120).availableSeats(45).price(new BigDecimal("2499.00"))
                .organizer(admin).build());

        log.info("Demo data seeded: 2 users, 7 events.");
    }
}
