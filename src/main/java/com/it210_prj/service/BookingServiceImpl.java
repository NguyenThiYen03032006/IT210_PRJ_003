package com.it210_prj.service;

import com.it210_prj.model.dto.BookingHistoryDTO;
import com.it210_prj.model.dto.BookingInvoiceDetailDTO;
import com.it210_prj.model.dto.BookingResponse;
import com.it210_prj.model.entity.Booking;
import com.it210_prj.model.entity.Movie;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    private static final int CANCEL_BEFORE_HOURS = 12;

    private final BookingRepository bookingRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;


    @Override
    @Transactional
    public BookingResponse bookTickets(String userEmail, Long showtimeId, List<Long> seatIds) {
        if (seatIds == null || seatIds.isEmpty()) {
            throw new RuntimeException("Vui long chon it nhat mot ghe.");
        }

        List<Long> uniqueSeatIds = new ArrayList<>(new LinkedHashSet<>(seatIds));
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new RuntimeException("Suat chieu khong ton tai."));

        if (!showtime.getStartTime().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Khong the dat ve cho suat chieu da bat dau hoac da ket thuc.");
        }

        List<Seat> seats = seatRepository.findByIdInAndRoomId(uniqueSeatIds, showtime.getRoom().getId());
        if (seats.size() != uniqueSeatIds.size()) {
            throw new RuntimeException("Co ghe khong thuoc phong chieu nay.");
        }

        List<Long> bookedSeatIds = ticketRepository.findBookedSeatIds(showtimeId, uniqueSeatIds);
        if (!bookedSeatIds.isEmpty()) {
            throw new RuntimeException("Mot hoac nhieu ghe da duoc dat: " + bookedSeatIds);
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Nguoi dung khong ton tai."));

        double total = 0;
        for (Seat seat : seats) {
            total += unitPriceForSeat(seat);
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setBookingTime(LocalDateTime.now());
        booking.setTotalPrice(total);
        booking.setStatus("PAID");
        bookingRepository.save(booking);

        for (Seat seat : seats) {
            Ticket ticket = new Ticket();
            ticket.setBooking(booking);
            ticket.setShowtime(showtime);
            ticket.setSeat(seat);
            ticket.setStatus("ACTIVE");
            ticket.setHoldKey(showtime.getId() + ":" + seat.getId());
            ticketRepository.save(ticket);
        }

        return new BookingResponse(booking.getId(), booking.getTotalPrice(), booking.getStatus());
    }

    @Override
    @Transactional
    public void requestCancelBooking(String userEmail, Long bookingId) {
        Booking booking = bookingRepository.findByIdAndUserEmail(bookingId, userEmail)
                .orElseThrow(() -> new RuntimeException("Khong tim thay hoa don cua ban."));

        if ("CANCELLED".equals(booking.getStatus())) {
            return;
        }

        if ("CONFIRMED".equals(booking.getStatus())) {
            throw new RuntimeException("Ve da in tai quay, khong the huy truc tuyen.");
        }

        if (!"PAID".equals(booking.getStatus())) {
            throw new RuntimeException("Trang thai don hien tai khong ho tro gui yeu cau huy.");
        }

        booking.setStatus("CANCEL_REQUESTED");
        bookingRepository.save(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingHistoryDTO> getHistory(String userEmail) {
        List<Ticket> tickets = ticketRepository.findHistoryTicketsByUserEmail(userEmail);
        return buildHistory(tickets);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingInvoiceDetailDTO getBookingInvoiceDetail(String userEmail, Long bookingId) {
        List<Ticket> tickets = ticketRepository.findInvoiceDetailForBooking(bookingId, userEmail);
        if (tickets.isEmpty()) {
            throw new RuntimeException("Khong tim thay hoa don hoac ban khong co quyen xem.");
        }
        Ticket first = tickets.get(0);
        Booking booking = first.getBooking();
        Showtime showtime = first.getShowtime();
        Movie movie = showtime.getMovie();
        User user = booking.getUser();

        List<String> seatNames = tickets.stream()
                .map(t -> t.getSeat().getSeatName())
                .toList();

        String fullName = user.getProfile() != null ? user.getProfile().getFullName() : null;
        String phone = user.getProfile() != null ? user.getProfile().getPhone() : null;
        String format = showtime.getScreenFormat() != null ? showtime.getScreenFormat() : "2D";

        return new BookingInvoiceDetailDTO(
                booking.getId(),
                booking.getBookingTime(),
                booking.getStatus(),
                booking.getTotalPrice(),
                movie.getTitle(),
                movie.getPoster(),
                movie.getDuration(),
                movie.getAgeRating(),
                showtime.getStartTime(),
                showtime.getEndTime(),
                showtime.getRoom().getName(),
                format,
                seatNames,
                user.getEmail(),
                fullName,
                phone
        );
    }


    @Override
    @Transactional(readOnly = true)
    public Page<BookingHistoryDTO> getStaffHistory(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> bookingPage;

        String q = keyword == null ? "" : keyword.trim();
        if (q.isEmpty()) {
            bookingPage = bookingRepository.findAllByOrderByBookingTimeDesc(pageable);
        } else if (q.matches("\\d+")) {
            Long bookingId = Long.parseLong(q);
            bookingPage = bookingRepository.findById(bookingId)
                    .map(b -> new PageImpl<>(List.of(b), pageable, 1))
                    .orElseGet(() -> new PageImpl<>(List.of(), pageable, 0));
        } else {
            bookingPage = bookingRepository.findByUserEmailContainingIgnoreCaseOrderByBookingTimeDesc(q, pageable);
        }

        List<Long> bookingIds = bookingPage.getContent().stream()
                .map(Booking::getId)
                .toList();
        Map<Long, List<Ticket>> ticketsByBookingId = new LinkedHashMap<>();
        if (!bookingIds.isEmpty()) {
            for (Ticket ticket : ticketRepository.findByBookingIdsWithDetails(bookingIds)) {
                ticketsByBookingId
                        .computeIfAbsent(ticket.getBooking().getId(), ignored -> new ArrayList<>())
                        .add(ticket);
            }
        }

        List<BookingHistoryDTO> rows = bookingPage.getContent().stream()
                .map(booking -> toHistoryRowForStaff(booking, ticketsByBookingId.get(booking.getId())))
                .toList();

        return new PageImpl<>(rows, pageable, bookingPage.getTotalElements());
    }

    @Override
    @Transactional
    public void confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay don dat ve."));

        if ("CANCELLED".equals(booking.getStatus())) {
            throw new RuntimeException("Don da huy, khong the in ve.");
        }
        if ("CONFIRMED".equals(booking.getStatus())) {
            return;
        }
        booking.setStatus("CONFIRMED");
        bookingRepository.save(booking);
    }


    @Override
    @Transactional
    public void cancelBookingByStaff(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay dat ve."));

        if ("CANCELLED".equals(booking.getStatus())) {
            return;
        }

        if ("CONFIRMED".equals(booking.getStatus())) {
            throw new RuntimeException("Ve da in tai quay, khong the huy.");
        }

        List<Ticket> tickets = ticketRepository.findByBookingId(bookingId);
        for (Ticket ticket : tickets) {
            LocalDateTime deadline = LocalDateTime.now().plusHours(CANCEL_BEFORE_HOURS);
            if (!deadline.isBefore(ticket.getShowtime().getStartTime())) {
                throw new RuntimeException(
                        "Khong the huy/hoan tien vi con duoi " + CANCEL_BEFORE_HOURS + " gio truoc suat chieu.");
            }
        }

        booking.setStatus("CANCELLED");
        for (Ticket ticket : tickets) {
            ticket.setStatus("CANCELLED");
            ticket.setHoldKey(null);
        }
        ticketRepository.saveAll(tickets);
        bookingRepository.save(booking);
    }

    private List<BookingHistoryDTO> buildHistory(List<Ticket> tickets) {
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


    private BookingHistoryDTO toHistoryRowForStaff(Booking booking, List<Ticket> bookingTickets) {
        if (bookingTickets == null || bookingTickets.isEmpty()) {
            return new BookingHistoryDTO(
                    booking.getId(),
                    "(Da huy - ve da duoc giai phong)",
                    null,
                    null,
                    "N/A",
                    List.of(),
                    booking.getTotalPrice(),
                    booking.getStatus(),
                    booking.getBookingTime()
            );
        }
        Ticket first = bookingTickets.get(0);
        Showtime showtime = first.getShowtime();
        List<String> seatNames = bookingTickets.stream()
                .map(ticket -> ticket.getSeat().getSeatName())
                .toList();
        return new BookingHistoryDTO(
                booking.getId(),
                showtime.getMovie().getTitle(),
                showtime.getMovie().getPoster(),
                showtime.getStartTime(),
                showtime.getRoom().getName(),
                seatNames,
                booking.getTotalPrice(),
                booking.getStatus(),
                booking.getBookingTime()
        );
    }

    private double unitPriceForSeat(Seat seat) {
        String t = seat.getSeatType();
        if (t == null) {
            return 75_000;
        }
        return switch (t.toUpperCase()) {
            case "VIP" -> 95_000;
            case "COUPLE" -> 140_000;
            default -> 75_000;
        };
    }
}
