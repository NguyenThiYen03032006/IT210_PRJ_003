package com.it210_prj.repository;

import com.it210_prj.model.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByIdAndUserEmail(Long id, String email);

    List<Booking> findByUserEmailOrderByBookingTimeDesc(String email);

    Page<Booking> findAllByOrderByBookingTimeDesc(Pageable pageable);

    Page<Booking> findByUserEmailContainingIgnoreCaseOrderByBookingTimeDesc(String email, Pageable pageable);

    long countByStatus(String status);

    @Query("""
            SELECT COUNT(b)
            FROM Booking b
            WHERE b.status IN ('PAID','CONFIRMED')
            """)
    long countActiveBookings();

    @Query("""
            SELECT COALESCE(SUM(b.totalPrice), 0)
            FROM Booking b
            WHERE b.status IN ('PAID','CONFIRMED')
            """)
    Double sumTotalPriceActiveBookings();

    @Query("""
            SELECT COALESCE(SUM(b.totalPrice), 0)
            FROM Booking b
            WHERE b.status IN ('PAID','CONFIRMED')
            AND b.bookingTime >= :from
            AND b.bookingTime < :toExclusive
            """)
    Double sumTotalPriceActiveBookingsInRange(
            @Param("from") LocalDateTime from,
            @Param("toExclusive") LocalDateTime toExclusive
    );

    @Query("""
            SELECT COUNT(b)
            FROM Booking b
            WHERE b.bookingTime >= :from
            AND b.bookingTime < :toExclusive
            AND b.status IN ('PAID','CONFIRMED')
            """)
    long countActiveBookingsInRange(
            @Param("from") LocalDateTime from,
            @Param("toExclusive") LocalDateTime toExclusive
    );
}
