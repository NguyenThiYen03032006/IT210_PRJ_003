package com.it210_prj.model.dto;

import lombok.Getter;

@Getter
public class MovieTicketStatDTO {

    private final String movieTitle;
    private final long ticketCount;

    public MovieTicketStatDTO(String movieTitle, Long ticketCount) {
        this.movieTitle = movieTitle;
        this.ticketCount = ticketCount != null ? ticketCount : 0L;
    }
}
