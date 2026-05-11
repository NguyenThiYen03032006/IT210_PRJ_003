package com.it210_prj.service.admin;

import com.it210_prj.model.entity.Movie;
import com.it210_prj.model.entity.Room;
import com.it210_prj.model.entity.Showtime;
import com.it210_prj.repository.MovieRepository;
import com.it210_prj.repository.RoomRepository;
import com.it210_prj.repository.ShowtimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Quản lý suất chiếu: thời lượng phim, đệm 15 phút giữa suất cùng phòng, và phân trang/lọc admin.
 */
@Service
public class ShowtimeService {

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private RoomRepository roomRepository;

    /**
     * Tạo suất: bắt buộc trong tương lai; endTime = start + duration phim;
     * cửa sổ xung đột = [start−15p, end+15p] để không chồng lịch phòng.
     */
    public void createShowtime(Long movieId, Long roomId, LocalDateTime startTime, String screenFormat) {
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
        showtime.setScreenFormat(
                screenFormat != null && !screenFormat.isBlank() ? screenFormat.trim() : "2D"
        );

        showtimeRepository.save(showtime);
    }

    /**
     * Cập nhật suất giữ nguyên logic create nhưng loại trừ chính showtimeId khỏi kiểm tra trùng phòng.
     */
    public void updateShowtime(Long showtimeId, Long movieId, Long roomId, LocalDateTime startTime, String screenFormat) {
        if (!startTime.isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Showtime must be in the future");
        }

        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new RuntimeException("Showtime not found"));

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found"));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        LocalDateTime endTime = startTime.plusMinutes(movie.getDuration());
        LocalDateTime conflictStart = startTime.minusMinutes(15);
        LocalDateTime conflictEnd = endTime.plusMinutes(15);

        List<Showtime> conflicts = showtimeRepository.findConflictExcludingId(
                showtimeId,
                roomId,
                conflictStart,
                conflictEnd
        );

        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Room is already booked in this time slot!");
        }

        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(startTime);
        showtime.setEndTime(endTime);
        showtime.setScreenFormat(
                screenFormat != null && !screenFormat.isBlank() ? screenFormat.trim() : "2D"
        );
        if (showtime.getStatus() == null || showtime.getStatus().isBlank()) {
            showtime.setStatus("ACTIVE");
        }

        showtimeRepository.save(showtime);
    }

    /** Chỉ xóa khi chưa có vé đặt (đếm ticket của suất). */
    public void deleteShowtime(Long showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new RuntimeException("Showtime not found"));

        long bookedSeats = showtimeRepository.countBookedSeats(showtimeId);
        if (bookedSeats > 0) {
            throw new RuntimeException("Khong the xoa suat chieu da co ve duoc dat.");
        }

        showtimeRepository.delete(showtime);
    }

    /** Danh sách đầy đủ (không lọc); dùng cho báo cáo đơn giản hoặc tích hợp khác. */
    public List<Showtime> findAll() {
        return showtimeRepository.findAll();
    }

    /**
     * Danh sách admin có phân trang: lọc tùy chọn theo phim, phòng, tên phim (LIKE),
     * và khoảng ngày dựa trên {@code startTime} (toDate inclusive đến hết ngày).
     */
    @Transactional(readOnly = true)
    public Page<Showtime> findAdminPage(
            Long movieId,
            Long roomId,
            String keyword,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    ) {
        Specification<Showtime> spec = (root, cq, cb) -> cb.conjunction();

        if (movieId != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("movie").get("id"), movieId));
        }
        if (roomId != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("room").get("id"), roomId));
        }
        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            spec = spec.and((root, cq, cb) -> cb.like(cb.lower(root.get("movie").get("title")), pattern));
        }
        if (fromDate != null) {
            LocalDateTime start = fromDate.atStartOfDay();
            spec = spec.and((root, cq, cb) -> cb.greaterThanOrEqualTo(root.get("startTime"), start));
        }
        if (toDate != null) {
            LocalDateTime endExclusive = toDate.plusDays(1).atStartOfDay();
            spec = spec.and((root, cq, cb) -> cb.lessThan(root.get("startTime"), endExclusive));
        }

        return showtimeRepository.findAll(spec, pageable);
    }

    /** Suất có startTime sau thời điểm hiện tại (theo query repository). */
    public List<Showtime> findUpcoming() {
        return showtimeRepository.findUpcomingShowtimes();
    }



}
