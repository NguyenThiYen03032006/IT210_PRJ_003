package com.it210_prj.controller.admin;

import com.it210_prj.repository.MovieRepository;
import com.it210_prj.repository.RoomRepository;
import com.it210_prj.service.admin.ShowtimeService;
import com.it210_prj.model.entity.Showtime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Controller
@RequestMapping("/admin/showtimes")
@RequiredArgsConstructor
public class ShowtimeController {

    private static final int SHOWTIME_PAGE_SIZE = 5;

    private final ShowtimeService showtimeService;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;


    @GetMapping
    public String list(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String movieId,
            @RequestParam(required = false) String roomId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        page = Math.max(0, page);

        Long movieIdLong = parseOptionalLong(movieId);
        Long roomIdLong = parseOptionalLong(roomId);
        String keyword = (q != null && !q.isBlank()) ? q.trim() : null;

        Pageable pageable = PageRequest.of(
                page,
                SHOWTIME_PAGE_SIZE,
                Sort.by(Sort.Direction.ASC, "startTime")
        );
        Page<Showtime> showtimePage = showtimeService.findAdminPage(
                movieIdLong,
                roomIdLong,
                keyword,
                fromDate,
                toDate,
                pageable
        );

        model.addAttribute("showtimePage", showtimePage);
        model.addAttribute("showtimes", showtimePage.getContent());
        model.addAttribute("movies", movieRepository.findAll());
        model.addAttribute("rooms", roomRepository.findAll());

        model.addAttribute("filterMovieId", movieIdLong);
        model.addAttribute("filterRoomId", roomIdLong);
        model.addAttribute("filterQ", keyword);
        model.addAttribute("filterFromDate", fromDate);
        model.addAttribute("filterToDate", toDate);

        return "admin/showtime/list";
    }

    private static Long parseOptionalLong(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @PostMapping("/create")
    public String create(
            @RequestParam Long movieId,
            @RequestParam Long roomId,
            @RequestParam String startTime,
            @RequestParam(required = false, defaultValue = "2D") String screenFormat,
            RedirectAttributes redirectAttributes
    ) {
        try {
            LocalDateTime time = LocalDateTime.parse(startTime);
            showtimeService.createShowtime(movieId, roomId, time, screenFormat);
            redirectAttributes.addFlashAttribute("successMessage", "Them suat chieu thanh cong.");
        } catch (DateTimeParseException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ngay gio bat dau khong hop le.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/showtimes";
    }

    @PostMapping("/{showtimeId}/update")
    public String update(
            @PathVariable Long showtimeId,
            @RequestParam Long movieId,
            @RequestParam Long roomId,
            @RequestParam String startTime,
            @RequestParam(required = false, defaultValue = "2D") String screenFormat,
            RedirectAttributes redirectAttributes
    ) {
        try {
            LocalDateTime time = LocalDateTime.parse(startTime);
            showtimeService.updateShowtime(showtimeId, movieId, roomId, time, screenFormat);
            redirectAttributes.addFlashAttribute("successMessage", "Cap nhat suat chieu thanh cong.");
        } catch (DateTimeParseException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ngay gio bat dau khong hop le.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/showtimes";
    }

    // Xóa suất chỉ khi chưa có vé
    @PostMapping("/{showtimeId}/delete")
    public String delete(
            @PathVariable Long showtimeId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            showtimeService.deleteShowtime(showtimeId);
            redirectAttributes.addFlashAttribute("successMessage", "Xoa suat chieu thanh cong.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/showtimes";
    }
}
