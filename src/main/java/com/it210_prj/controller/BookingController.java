package com.it210_prj.controller;

import com.it210_prj.model.dto.BookingHistoryDTO;
import com.it210_prj.model.dto.BookingRequest;
import com.it210_prj.model.dto.BookingResponse;
import com.it210_prj.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> cancel(
            Authentication authentication,
            @PathVariable Long bookingId
    ) {

        try {

            bookingService.requestCancelBooking(
                    authentication.getName(),
                    bookingId
            );

            return ResponseEntity.ok("Đã gửi yêu cầu hủy vé");

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/history")
    public List<BookingHistoryDTO> history(Authentication authentication) {
        return bookingService.getHistory(authentication.getName());
    }
}
