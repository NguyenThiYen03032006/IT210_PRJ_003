package com.it210_prj.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BookingRequest {

    @NotNull(message = "Ma suat chieu khong duoc de trong")
    private Long showtimeId;

    @NotEmpty(message = "Danh sach ghe khong duoc de trong")
    private List<Long> seatIds;
}
