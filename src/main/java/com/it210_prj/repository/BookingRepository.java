package com.it210_prj.repository;

import com.it210_prj.model.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByIdAndUserEmail(Long id, String email);

    List<Booking> findByUserEmailOrderByBookingTimeDesc(String email);
}
