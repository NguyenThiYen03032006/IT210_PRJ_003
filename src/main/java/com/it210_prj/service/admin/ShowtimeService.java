package com.it210_prj.service.admin;

import com.it210_prj.model.entity.Movie;
import com.it210_prj.model.entity.Room;
import com.it210_prj.model.entity.Showtime;
import com.it210_prj.repository.MovieRepository;
import com.it210_prj.repository.RoomRepository;
import com.it210_prj.repository.ShowtimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShowtimeService {

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private RoomRepository roomRepository;

    public void createShowtime(Long movieId, Long roomId, LocalDateTime startTime) {
        if (!startTime.isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Showtime must be in the future");
        }

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found"));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        LocalDateTime endTime = startTime.plusMinutes(movie.getDuration());
        LocalDateTime conflictStart = startTime.minusMinutes(15);
        LocalDateTime conflictEnd = endTime.plusMinutes(15);

        List<Showtime> conflicts = showtimeRepository.findConflict(
                roomId,
                conflictStart,
                conflictEnd
        );

        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Room is already booked in this time slot!");
        }

        Showtime showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(startTime);
        showtime.setEndTime(endTime);
        showtime.setStatus("ACTIVE");

        showtimeRepository.save(showtime);
    }

    public List<Showtime> findAll() {
        return showtimeRepository.findAll();
    }

    public List<Showtime> findUpcoming() {
        return showtimeRepository.findUpcomingShowtimes();
    }
}
