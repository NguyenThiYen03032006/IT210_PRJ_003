package com.it210_prj.repository;

import com.it210_prj.model.dto.MovieTicketStatDTO;
import com.it210_prj.model.entity.Ticket;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("""
        SELECT COUNT(t)
        FROM Ticket t
        WHERE t.showtime.id = :showtimeId
        AND (t.status IS NULL OR t.status IN ('ACTIVE', 'AVTIVE'))
        """)
    long countByShowtimeId(@Param("showtimeId") Long showtimeId);

    List<Ticket> findByBookingId(Long bookingId);

    @Query("""
        SELECT t
        FROM Ticket t
        JOIN FETCH t.booking b
        JOIN FETCH t.showtime st
        JOIN FETCH st.movie
        JOIN FETCH st.room
        JOIN FETCH t.seat
        WHERE b.id IN :bookingIds
        ORDER BY b.bookingTime DESC, t.seat.seatName ASC
    """)
    List<Ticket> findByBookingIdsWithDetails(@Param("bookingIds") Collection<Long> bookingIds);

    @Query("""
        SELECT t.seat.id
        FROM Ticket t
        WHERE t.showtime.id = :showtimeId
        AND t.seat.id IN :seatIds
        AND (t.status IS NULL OR t.status IN ('ACTIVE', 'AVTIVE'))
    """)
    List<Long> findBookedSeatIds(
            @Param("showtimeId") Long showtimeId,
            @Param("seatIds") Collection<Long> seatIds
    );

    @Query("""
        SELECT t.seat.id
        FROM Ticket t
        WHERE t.showtime.id = :showtimeId
        AND (t.status IS NULL OR t.status IN ('ACTIVE', 'AVTIVE'))
    """)
    List<Long> findBookedSeatIdsByShowtimeId(@Param("showtimeId") Long showtimeId);

    @Query("""
        SELECT t
        FROM Ticket t
        JOIN FETCH t.booking b
        JOIN FETCH t.showtime st
        JOIN FETCH st.movie
        JOIN FETCH st.room
        JOIN FETCH t.seat
        WHERE b.user.email = :email
        ORDER BY b.bookingTime DESC, t.seat.seatName ASC
    """)
    List<Ticket> findHistoryTicketsByUserEmail(@Param("email") String email);

    @Query("""
        SELECT t
        FROM Ticket t
        JOIN FETCH t.booking b
        JOIN FETCH b.user
        JOIN FETCH t.showtime st
        JOIN FETCH st.movie
        JOIN FETCH st.room
        JOIN FETCH t.seat
        ORDER BY b.bookingTime DESC, t.seat.seatName ASC
    """)
    List<Ticket> findAllHistoryTickets();

    /**
     * Chi tiết hóa đơn: JOIN Ticket ↔ Booking ↔ User, Showtime ↔ Movie ↔ Room, Seat.
     */
    @Query("""
        SELECT t
        FROM Ticket t
        JOIN FETCH t.booking b
        JOIN FETCH b.user u
        JOIN FETCH t.showtime st
        JOIN FETCH st.movie m
        JOIN FETCH st.room r
        JOIN FETCH t.seat s
        WHERE b.id = :bookingId AND u.email = :email
        ORDER BY s.seatName ASC
        """)
    List<Ticket> findInvoiceDetailForBooking(
            @Param("bookingId") Long bookingId,
            @Param("email") String email
    );

    @Query("""
            SELECT new com.it210_prj.model.dto.MovieTicketStatDTO(m.title, COUNT(t.id))
            FROM Ticket t
            JOIN t.showtime st
            JOIN st.movie m
            WHERE (t.status IS NULL OR t.status IN ('ACTIVE', 'AVTIVE'))
            GROUP BY m.id, m.title
            ORDER BY COUNT(t.id) DESC
            """)
    List<MovieTicketStatDTO> findTopMoviesByTicketCount(Pageable pageable);
}
