package com.it210_prj.repository;

import com.it210_prj.model.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ShowtimeRepository extends JpaRepository<Showtime, Long>, JpaSpecificationExecutor<Showtime> {

    // Lấy suất chiếu theo phòng
    List<Showtime> findByRoomIdOrderByStartTimeAsc(Long roomId);

    // Lấy suất chiếu theo phim
    List<Showtime> findByMovieIdOrderByStartTimeAsc(Long movieId);
    boolean existsByMovieId(Long movieId);

    // Kiểm tra xung đột phòng (overlap)
    @Query("""
        SELECT s FROM Showtime s
        WHERE s.room.id = :roomId
        AND (
            (:startTime < s.endTime)
            AND (:endTime > s.startTime)
        )
    """)
    List<Showtime> findConflict(
            @Param("roomId") Long roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("""
        SELECT s FROM Showtime s
        WHERE s.room.id = :roomId
        AND s.id <> :showtimeId
        AND (
            (:startTime < s.endTime)
            AND (:endTime > s.startTime)
        )
    """)
    List<Showtime> findConflictExcludingId(
            @Param("showtimeId") Long showtimeId,
            @Param("roomId") Long roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // Suất chiếu sắp tới
    @Query("""
        SELECT s FROM Showtime s
        WHERE s.startTime > CURRENT_TIMESTAMP
        ORDER BY s.startTime ASC
    """)
    List<Showtime> findUpcomingShowtimes();

    // Suất chiếu đã kết thúc
    @Query("""
        SELECT s FROM Showtime s
        WHERE s.endTime < CURRENT_TIMESTAMP
    """)
    List<Showtime> findExpiredShowtimes();

    // Đếm số ghế đã đặt
    @Query("""
        SELECT COUNT(t.id)
        FROM Ticket t
        WHERE t.showtime.id = :showtimeId
    """)
    long countBookedSeats(@Param("showtimeId") Long showtimeId);

    long countByStartTimeAfter(LocalDateTime instant);

    long countByEndTimeBefore(LocalDateTime instant);
}
