package com.it210_prj.service.admin;

import com.it210_prj.model.dto.AdminDashboardStats;
import com.it210_prj.model.entity.Role;
import com.it210_prj.repository.BookingRepository;
import com.it210_prj.repository.MovieRepository;
import com.it210_prj.repository.RoomRepository;
import com.it210_prj.repository.SeatRepository;
import com.it210_prj.repository.ShowtimeRepository;
import com.it210_prj.repository.TicketRepository;
import com.it210_prj.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Gom các chỉ số đếm/doanh thu từ repository để hiển thị dashboard admin (read-only). */
@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final TicketRepository ticketRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    /**
     * Tính snapshot hệ thống: số lượng thực thể, booking và vé, doanh thu tích lũy và trong ngày,
     * top phim theo số vé (giới hạn 5).
     */
    @Transactional(readOnly = true)
    public AdminDashboardStats loadStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        LocalDateTime nextDay = dayStart.plusDays(1);

        double totalRevenue = nz(bookingRepository.sumTotalPriceActiveBookings());
        double todayRevenue = nz(bookingRepository.sumTotalPriceActiveBookingsInRange(dayStart, nextDay));

        return AdminDashboardStats.builder()
                .movieCount(movieRepository.count())
                .roomCount(roomRepository.count())
                .seatCount(seatRepository.count())
                .showtimeTotal(showtimeRepository.count())
                .upcomingShowtimeCount(showtimeRepository.countByStartTimeAfter(now))
                .pastShowtimeCount(showtimeRepository.countByEndTimeBefore(now))
                .customerCount(userRepository.countByRole(Role.CUSTOMER))
                .staffCount(userRepository.countByRole(Role.STAFF))
                .bookingTotal(bookingRepository.count())
                .bookingActiveCount(bookingRepository.countActiveBookings())
                .bookingCancelledCount(bookingRepository.countByStatus("CANCELLED"))
                .bookingsToday(bookingRepository.countActiveBookingsInRange(dayStart, nextDay))
                .totalRevenue(totalRevenue)
                .todayRevenue(todayRevenue)
                .ticketsSoldTotal(ticketRepository.count())
                .topMoviesByTickets(ticketRepository.findTopMoviesByTicketCount(PageRequest.of(0, 5)))
                .build();
    }

    /** Helper: SUM JPQL có thể null khi không có bản ghi. */
    private static double nz(Double v) {
        return v != null ? v : 0.0;
    }
}
