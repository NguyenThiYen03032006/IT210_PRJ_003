package com.it210_prj.service;

import com.it210_prj.model.dto.BookingHistoryDTO;
import com.it210_prj.model.dto.BookingResponse;

import java.util.List;

public interface BookingService {

    BookingResponse bookTickets(String userEmail, Long showtimeId, List<Long> seatIds);

    void cancelBooking(String userEmail, Long bookingId);

    List<BookingHistoryDTO> getHistory(String userEmail);
}
