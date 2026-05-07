package com.it210_prj.service;

import com.it210_prj.model.dto.BookingHistoryDTO;
import com.it210_prj.model.dto.BookingResponse;
import com.it210_prj.model.entity.Booking;
import com.it210_prj.model.entity.Seat;
import com.it210_prj.model.entity.Showtime;
import com.it210_prj.model.entity.Ticket;
import com.it210_prj.model.entity.User;
import com.it210_prj.repository.BookingRepository;
import com.it210_prj.repository.SeatRepository;
import com.it210_prj.repository.ShowtimeRepository;
import com.it210_prj.repository.TicketRepository;
import com.it210_prj.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final double TICKET_PRICE = 75000D;
    private static final int CANCEL_BEFORE_HOURS = 24;

    private final BookingRepository bookingRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingResponse bookTickets(String userEmail, Long showtimeId, List<Long> seatIds) {
        if (seatIds == null || seatIds.isEmpty()) {
            throw new RuntimeException("Vui lòng chọn ít nhất một ghế");
        }

        List<Long> uniqueSeatIds = new ArrayList<>(new LinkedHashSet<>(seatIds));
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new RuntimeException("Suất chiếu không tồn tại"));

        if (!showtime.getStartTime().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Không thể đặt vé cho suất chiếu đã bắt đầu hoặc đã kết thúc");
        }

        List<Seat> seats = seatRepository.findByIdInAndRoomId(uniqueSeatIds, showtime.getRoom().getId());
        if (seats.size() != uniqueSeatIds.size()) {
            throw new RuntimeException("Có ghế không thuộc phòng chiếu này");
        }

        List<Long> bookedSeatIds = ticketRepository.findBookedSeatIds(showtimeId, uniqueSeatIds);
        if (!bookedSeatIds.isEmpty()) {
            throw new RuntimeException("Một hoặc nhiều ghế đã được đặt: " + bookedSeatIds);
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setBookingTime(LocalDateTime.now());
        booking.setTotalPrice(uniqueSeatIds.size() * TICKET_PRICE);
        booking.setStatus("PAID");
        bookingRepository.save(booking);

        for (Seat seat : seats) {
            Ticket ticket = new Ticket();
            ticket.setBooking(booking);
            ticket.setShowtime(showtime);
            ticket.setSeat(seat);
            ticketRepository.save(ticket);
        }

        return new BookingResponse(booking.getId(), booking.getTotalPrice(), booking.getStatus());
    }

    @Override
    @Transactional
    public void cancelBooking(String userEmail, Long bookingId) {
        Booking booking = bookingRepository.findByIdAndUserEmail(bookingId, userEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn của bạn"));

        if ("CANCELLED".equals(booking.getStatus())) {
            return;
        }

        List<Ticket> tickets = ticketRepository.findByBookingId(bookingId);
        for (Ticket ticket : tickets) {
            LocalDateTime deadline = LocalDateTime.now().plusHours(CANCEL_BEFORE_HOURS);
            if (!deadline.isBefore(ticket.getShowtime().getStartTime())) {
                throw new RuntimeException("Chỉ được hủy vé trước giờ chiếu ít nhất 24 giờ");
            }
        }

        booking.setStatus("CANCELLED");
        ticketRepository.deleteByBookingId(bookingId);
        bookingRepository.save(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingHistoryDTO> getHistory(String userEmail) {
        List<Ticket> tickets = ticketRepository.findHistoryTicketsByUserEmail(userEmail);
        Map<Long, List<Ticket>> ticketsByBooking = new LinkedHashMap<>();

        for (Ticket ticket : tickets) {
            ticketsByBooking
                    .computeIfAbsent(ticket.getBooking().getId(), ignored -> new ArrayList<>())
                    .add(ticket);
        }

        List<BookingHistoryDTO> history = new ArrayList<>();
        for (List<Ticket> bookingTickets : ticketsByBooking.values()) {
            Ticket first = bookingTickets.get(0);
            Booking booking = first.getBooking();
            Showtime showtime = first.getShowtime();
            List<String> seatNames = bookingTickets.stream()
                    .map(ticket -> ticket.getSeat().getSeatName())
                    .toList();

            history.add(new BookingHistoryDTO(
                    booking.getId(),
                    showtime.getMovie().getTitle(),
                    showtime.getMovie().getPoster(),
                    showtime.getStartTime(),
                    showtime.getRoom().getName(),
                    seatNames,
                    booking.getTotalPrice(),
                    booking.getStatus(),
                    booking.getBookingTime()
            ));
        }

        return history;
    }
}
