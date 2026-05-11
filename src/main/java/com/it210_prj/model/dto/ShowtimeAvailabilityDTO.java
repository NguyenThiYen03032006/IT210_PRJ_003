package com.it210_prj.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ShowtimeAvailabilityDTO {

    private Long showtimeId;
    private LocalDateTime startTime;
    private String roomName;
    private String screenFormat;
    /** true khi số vé = số ghế phòng (full). */
    private boolean soldOut;
}
