package com.it210_prj.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class BookingInvoiceDetailDTO {

    private Long bookingId;
    private LocalDateTime bookingTime;
    private String status;
    private Double totalPrice;

    private String movieTitle;
    private String poster;
    private Integer movieDuration;
    private String ageRating;

    private LocalDateTime showtimeStart;
    private LocalDateTime showtimeEnd;
    private String roomName;
    private String screenFormat;

    private List<String> seatNames;

    private String customerEmail;
    private String customerFullName;
    private String customerPhone;
}
