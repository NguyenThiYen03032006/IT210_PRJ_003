package com.it210_prj.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BookingResponse {

    private Long bookingId;
    private Double totalPrice;
    private String status;
}
