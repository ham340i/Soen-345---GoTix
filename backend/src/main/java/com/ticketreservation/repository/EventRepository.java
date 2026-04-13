package com.ticketreservation.repository;

import com.ticketreservation.model.Event;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * Acquires a pessimistic write lock on the event row before reading it.
     * Used exclusively inside the reservation transaction so that concurrent
     * bookings for the same event are serialised at the DB level, preventing
     * double-booking beyond availableSeats even under high load.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Event e WHERE e.id = :id")
    Optional<Event> findByIdForUpdate(@Param("id") Long id);

    List<Event> findByStatusOrderByEventDateAsc(Event.EventStatus status);

    List<Event> findByCategoryAndStatusOrderByEventDateAsc(Event.EventCategory category, Event.EventStatus status);

    @Query("SELECT e FROM Event e WHERE e.status = 'ACTIVE' AND " +
           "LOWER(e.location) LIKE LOWER(CONCAT('%', :location, '%')) ORDER BY e.eventDate ASC")
    List<Event> findByLocationContainingIgnoreCase(@Param("location") String location);

    @Query("SELECT e FROM Event e WHERE e.status = 'ACTIVE' AND " +
           "e.eventDate BETWEEN :startDate AND :endDate ORDER BY e.eventDate ASC")
    List<Event> findByEventDateBetween(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e FROM Event e WHERE e.status = 'ACTIVE' AND " +
           "(LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.location) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY e.eventDate ASC")
    List<Event> searchEvents(@Param("keyword") String keyword);

    @Query("SELECT e FROM Event e WHERE e.status = 'ACTIVE' AND " +
           "(:category IS NULL OR e.category = :category) AND " +
           "(:location IS NULL OR LOWER(e.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:startDate IS NULL OR e.eventDate >= :startDate) AND " +
           "(:endDate IS NULL OR e.eventDate <= :endDate) ORDER BY e.eventDate ASC")
    List<Event> filterEvents(@Param("category") Event.EventCategory category,
                              @Param("location") String location,
                              @Param("startDate") LocalDateTime startDate,
                              @Param("endDate") LocalDateTime endDate);

    List<Event> findByOrganizerIdOrderByCreatedAtDesc(Long organizerId);
}
