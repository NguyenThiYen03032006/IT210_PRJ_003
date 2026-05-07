package com.it210_prj.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class BookingHistoryDTO {

    private Long bookingId;
    private String movieTitle;
    private String poster;
    private LocalDateTime startTime;
    private String roomName;
    private List<String> seatNames;
    private Double totalPrice;
    private String status;
    private LocalDateTime bookingTime;
}
