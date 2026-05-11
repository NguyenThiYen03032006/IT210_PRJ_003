package com.it210_prj.repository;

import com.it210_prj.model.entity.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Collection;
import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    long countByRoomId(Long roomId);

    List<Seat> findByRoomIdOrderBySeatNameAsc(Long roomId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Seat> findByIdInAndRoomId(Collection<Long> ids, Long roomId);
}
