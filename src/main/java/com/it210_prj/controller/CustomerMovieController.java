package com.it210_prj.controller;

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

import java.util.List;

@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerMovieController {

    private final MovieService movieService;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final BookingService bookingService;

    @GetMapping("/movies/{movieId}")
    public String movieDetail(@PathVariable Long movieId, Model model) {
        model.addAttribute("movie", movieService.findById(movieId));
        model.addAttribute("showtimes", showtimeRepository.findByMovieIdOrderByStartTimeAsc(movieId));
        return "customer/movie/detail";
    }

    @GetMapping("/showtimes/{showtimeId}/seats")
    public String seatMap(@PathVariable Long showtimeId, Model model) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new RuntimeException("Showtime not found"));
        List<Seat> seats = seatRepository.findByRoomIdOrderBySeatNameAsc(showtime.getRoom().getId());

        model.addAttribute("showtime", showtime);
        model.addAttribute("seats", seats);
        model.addAttribute("bookedSeatIds", ticketRepository.findBookedSeatIdsByShowtimeId(showtimeId));
        return "customer/booking/seat-map";
    }

    @GetMapping("/bookings")
    public String bookingHistory(Authentication authentication, Model model) {
        model.addAttribute("bookings", bookingService.getHistory(authentication.getName()));
        return "customer/booking/history";
    }

    @PostMapping("/bookings/{bookingId}/cancel-form")
    public String cancelFromPage(Authentication authentication, @PathVariable Long bookingId) {
        bookingService.cancelBooking(authentication.getName(), bookingId);
        return "redirect:/customer/bookings";
    }
}
