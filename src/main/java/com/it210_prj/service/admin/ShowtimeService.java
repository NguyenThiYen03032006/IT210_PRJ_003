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

@Service
public class ShowtimeService {

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private RoomRepository roomRepository;

    public void createShowtime(Long movieId,
                               Long roomId,
                               LocalDateTime startTime,
                               String screenFormat) {

        // 1. Kiểm tra thời gian phải ở tương lai
        if (!startTime.isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Suat chieu phai duoc tao o thoi diem trong tuong lai.");
        }

        // 2. Tìm phim theo ID
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay phim."));

        // 3. Tìm phòng theo ID
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay phong chieu."));

        // 4. Tính thời gian kết thúc (start + thời lượng phim)
        LocalDateTime endTime = startTime.plusMinutes(movie.getDuration());

        // 5. Tạo khoảng kiểm tra xung đột (cộng/trừ 15 phút dọn phòng)
        LocalDateTime conflictStart = startTime.minusMinutes(15);
        LocalDateTime conflictEnd = endTime.plusMinutes(15);

        // 6. Kiểm tra có suất chiếu nào trùng phòng và khung giờ không
        List<Showtime> conflicts = showtimeRepository.findConflict(
                roomId,
                conflictStart,
                conflictEnd
        );

        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Phong da duoc dat trong khung gio nay.");
        }

        // 7. Tạo đối tượng suất chiếu mới
        Showtime showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(startTime);
        showtime.setEndTime(endTime);
        showtime.setStatus("ACTIVE");

        // Nếu không nhập định dạng màn hình thì mặc định là 2D
        showtime.setScreenFormat(
                screenFormat != null && !screenFormat.isBlank()
                        ? screenFormat.trim()
                        : "2D"
        );

        // 8. Lưu vào database
        showtimeRepository.save(showtime);
    }


    public void updateShowtime(Long showtimeId,
                               Long movieId,
                               Long roomId,
                               LocalDateTime startTime,
                               String screenFormat) {

        // 1. Kiểm tra thời gian phải ở tương lai
        if (!startTime.isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Thoi gian suat chieu phai o tuong lai.");
        }

        // 2. Tìm suất chiếu
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay suat chieu."));

        // 3. Tìm phim
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay phim."));

        // 4. Tìm phòng
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay phong chieu."));

        // 5. Tính lại thời gian kết thúc
        LocalDateTime endTime = startTime.plusMinutes(movie.getDuration());

        // 6. Khoảng kiểm tra xung đột (±15 phút)
        LocalDateTime conflictStart = startTime.minusMinutes(15);
        LocalDateTime conflictEnd = endTime.plusMinutes(15);

        // 7. Kiểm tra trùng phòng (loại trừ chính suất đang sửa)
        List<Showtime> conflicts = showtimeRepository.findConflictExcludingId(
                showtimeId,
                roomId,
                conflictStart,
                conflictEnd
        );

        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Phong da duoc dat trong khung gio nay.");
        }

        // 8. Cập nhật dữ liệu
        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(startTime);
        showtime.setEndTime(endTime);

        showtime.setScreenFormat(
                screenFormat != null && !screenFormat.isBlank()
                        ? screenFormat.trim()
                        : "2D"
        );

        // Nếu trạng thái chưa có thì đặt mặc định ACTIVE
        if (showtime.getStatus() == null || showtime.getStatus().isBlank()) {
            showtime.setStatus("ACTIVE");
        }

        // 9. Lưu lại
        showtimeRepository.save(showtime);
    }

    public void deleteShowtime(Long showtimeId) {

        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay suat chieu."));

        long bookedSeats = showtimeRepository.countBookedSeats(showtimeId);

        if (bookedSeats > 0) {
            throw new RuntimeException("Khong the xoa suat chieu vi da co ve duoc dat.");
        }

        showtimeRepository.delete(showtime);
    }

    public List<Showtime> findAll() {
        return showtimeRepository.findAll();
    }

    // Phân trang + lọc
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

        // Lọc theo phim
        if (movieId != null) {
            spec = spec.and((root, cq, cb) ->
                    cb.equal(root.get("movie").get("id"), movieId));
        }

        // Lọc theo phòng
        if (roomId != null) {
            spec = spec.and((root, cq, cb) ->
                    cb.equal(root.get("room").get("id"), roomId));
        }

        // Lọc theo tên phim (không phân biệt hoa thường)
        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            spec = spec.and((root, cq, cb) ->
                    cb.like(cb.lower(root.get("movie").get("title")), pattern));
        }

        // Lọc từ ngày
        if (fromDate != null) {
            LocalDateTime start = fromDate.atStartOfDay();
            spec = spec.and((root, cq, cb) ->
                    cb.greaterThanOrEqualTo(root.get("startTime"), start));
        }

        // Lọc đến ngày (bao gồm hết ngày đó)
        if (toDate != null) {
            LocalDateTime endExclusive = toDate.plusDays(1).atStartOfDay();
            spec = spec.and((root, cq, cb) ->
                    cb.lessThan(root.get("startTime"), endExclusive));
        }

        return showtimeRepository.findAll(spec, pageable);
    }


    public List<Showtime> findUpcoming() {
        return showtimeRepository.findUpcomingShowtimes();
    }

}