package com.it210_prj.controller;

import com.it210_prj.model.dto.BookingHistoryDTO;
import com.it210_prj.model.dto.BookingRequest;
import com.it210_prj.model.dto.BookingResponse;
import com.it210_prj.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/customer/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingResponse bookTickets(
            Authentication authentication,
            @Valid @RequestBody BookingRequest request
    ) {
        return bookingService.bookTickets(
                authentication.getName(),
                request.getShowtimeId(),
                request.getSeatIds()
        );
    }

    @PostMapping("/{bookingId}/cancel")
    public void cancel(Authentication authentication, @PathVariable Long bookingId) {
        bookingService.cancelBooking(authentication.getName(), bookingId);
    }

    @GetMapping("/history")
    public List<BookingHistoryDTO> history(Authentication authentication) {
        return bookingService.getHistory(authentication.getName());
    }
}
