package com.it210_prj.controller;

import com.it210_prj.model.dto.ShowtimeAvailabilityDTO;
import com.it210_prj.model.entity.Seat;
import com.it210_prj.model.entity.Showtime;
import com.it210_prj.repository.SeatRepository;
import com.it210_prj.repository.ShowtimeRepository;
import com.it210_prj.repository.TicketRepository;
import com.it210_prj.service.BookingService;
import com.it210_prj.service.admin.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerMovieController {

    private final MovieService movieService;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final BookingService bookingService;

    // Hiển thị chi tiết phim và danh sách suất chiếu còn trong tương lai.

    @GetMapping("/movies/{movieId}")
    public String movieDetail(@PathVariable Long movieId, Model model) {

        // Lấy thông tin phim và gửi sang view
        model.addAttribute("movie", movieService.findById(movieId));

        // Lấy tất cả suất chiếu của phim theo thứ tự tăng dần thời gian
        List<Showtime> showtimes =
                showtimeRepository.findByMovieIdOrderByStartTimeAsc(movieId);

        LocalDateTime now = LocalDateTime.now();

        // Map dùng để gom suất chiếu theo từng ngày
        // Key = ngày chiếu
        // Value = danh sách suất trong ngày đó
        Map<LocalDate, List<ShowtimeAvailabilityDTO>> showtimesByDate =
                new LinkedHashMap<>();

        for (Showtime st : showtimes) {

            // Bỏ qua các suất đã qua hoặc đang chiếu
            if (!st.getStartTime().isAfter(now)) {
                continue;
            }

            // Tổng số ghế trong phòng
            long capacity =
                    seatRepository.countByRoomId(st.getRoom().getId());

            // Số vé đã đặt cho suất này
            long booked =
                    ticketRepository.countByShowtimeId(st.getId());

            // Kiểm tra hết vé
            boolean soldOut = capacity > 0 && booked >= capacity;

            // Lấy định dạng màn chiếu (2D, 3D, IMAX...)
            String format = st.getScreenFormat() != null
                    ? st.getScreenFormat()
                    : "2D";

            // Tạo DTO để gửi sang view
            ShowtimeAvailabilityDTO row =
                    new ShowtimeAvailabilityDTO(
                            st.getId(),
                            st.getStartTime(),
                            st.getRoom().getName(),
                            format,
                            soldOut
                    );

            // Lấy ngày của suất chiếu
            LocalDate day = st.getStartTime().toLocalDate();

            // Gom suất chiếu vào đúng ngày
            showtimesByDate
                    .computeIfAbsent(day, d -> new ArrayList<>())
                    .add(row);
        }

        // Gửi dữ liệu sang view
        model.addAttribute("showtimesByDate", showtimesByDate);

        return "customer/movie/detail";
    }

  // Hiển thị sơ đồ ghế
    @GetMapping("/showtimes/{showtimeId}/seats")
    public String seatMap(
            @PathVariable Long showtimeId,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        // Tìm suất chiếu theo ID
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() ->
                        new RuntimeException("Showtime not found"));

        Long movieId = showtime.getMovie().getId();

        // Kiểm tra suất đã qua giờ chưa
        if (!showtime.getStartTime().isAfter(LocalDateTime.now())) {

            redirectAttributes.addFlashAttribute(
                    "infoMessage",
                    "Suất chiếu đã qua giờ, không thể đặt vé."
            );

            return "redirect:/customer/movies/" + movieId;
        }

        // Kiểm tra hết vé chưa
        long capacity =
                seatRepository.countByRoomId(showtime.getRoom().getId());

        long booked =
                ticketRepository.countByShowtimeId(showtimeId);

        if (capacity > 0 && booked >= capacity) {

            redirectAttributes.addFlashAttribute(
                    "infoMessage",
                    "Suất chiếu đã hết vé."
            );

            return "redirect:/customer/movies/" + movieId;
        }

        // Lấy danh sách ghế trong phòng
        List<Seat> seats =
                seatRepository.findByRoomIdOrderBySeatNameAsc(
                        showtime.getRoom().getId()
                );

        // Gửi dữ liệu sang view
        model.addAttribute("showtime", showtime);
        model.addAttribute("seats", seats);

        // Danh sách ID ghế đã đặt để disable trên giao diện
        model.addAttribute(
                "bookedSeatIds",
                ticketRepository.findBookedSeatIdsByShowtimeId(showtimeId)
        );

        return "customer/booking/seat-map";
    }

    // lịch sử
    @GetMapping("/bookings")
    public String bookingHistory(
            Authentication authentication,
            Model model
    ) {

        // Lấy username từ Spring Security
        String username = authentication.getName();

        // Lấy lịch sử booking của user
        model.addAttribute(
                "bookings",
                bookingService.getHistory(username)
        );

        return "customer/booking/history";
    }

  // hóa đơn
    @GetMapping("/bookings/{bookingId}")
    public String bookingInvoice(
            @PathVariable Long bookingId,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            model.addAttribute(
                    "invoice",
                    bookingService.getBookingInvoiceDetail(
                            authentication.getName(),
                            bookingId
                    )
            );

            return "customer/booking/invoice";

        } catch (RuntimeException e) {

            // Nếu booking không thuộc user hoặc lỗi khác
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            return "redirect:/customer/bookings";
        }
    }

 // yêu cầu hủy vé
    @PostMapping("/bookings/{bookingId}/cancel-form")
    public String cancelFromPage(
            Authentication authentication,
            @PathVariable Long bookingId,
            RedirectAttributes redirectAttributes
    ) {

        try {
            bookingService.requestCancelBooking(
                    authentication.getName(),
                    bookingId
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Da gui yeu cau huy ve. Nhan vien se xu ly tai quay."
            );

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/customer/bookings";
    }
}