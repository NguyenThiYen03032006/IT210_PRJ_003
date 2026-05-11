package com.it210_prj.model.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AdminDashboardStats {

    private long movieCount;
    private long roomCount;
    private long seatCount;
    private long showtimeTotal;
    private long upcomingShowtimeCount;
    private long pastShowtimeCount;

    private long customerCount;
    private long staffCount;

    private long bookingTotal;
    private long bookingActiveCount;
    private long bookingCancelledCount;
    private long bookingsToday;

    private double totalRevenue;
    private double todayRevenue;

    private long ticketsSoldTotal;

    private List<MovieTicketStatDTO> topMoviesByTickets;
}
