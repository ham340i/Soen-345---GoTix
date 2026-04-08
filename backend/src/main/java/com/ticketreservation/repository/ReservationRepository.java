package com.ticketreservation.repository;

import com.ticketreservation.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserIdOrderByReservedAtDesc(Long userId);

    List<Reservation> findByEventIdOrderByReservedAtDesc(Long eventId);

    Optional<Reservation> findByConfirmationCode(String confirmationCode);

    List<Reservation> findByUserIdAndStatus(Long userId, Reservation.ReservationStatus status);

    boolean existsByUserIdAndEventIdAndStatus(Long userId, Long eventId, Reservation.ReservationStatus status);
}
