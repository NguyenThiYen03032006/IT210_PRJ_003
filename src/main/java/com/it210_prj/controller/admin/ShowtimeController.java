package com.it210_prj.controller.admin;

import com.it210_prj.repository.MovieRepository;
import com.it210_prj.repository.RoomRepository;
import com.it210_prj.service.admin.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Controller
@RequestMapping("/admin/showtimes")
@RequiredArgsConstructor
public class ShowtimeController {
    private final ShowtimeService showtimeService;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("showtimes", showtimeService.findAll());
        model.addAttribute("movies", movieRepository.findAll());
        model.addAttribute("rooms", roomRepository.findAll());
        return "admin/showtime/list";
    }

    @PostMapping("/create")
    public String create(
            @RequestParam Long movieId,
            @RequestParam Long roomId,
            @RequestParam String startTime,
            RedirectAttributes redirectAttributes
    ) {
        try {
            LocalDateTime time = LocalDateTime.parse(startTime);
            showtimeService.createShowtime(movieId, roomId, time);
            redirectAttributes.addFlashAttribute("successMessage", "Them suat chieu thanh cong.");
        } catch (DateTimeParseException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ngay gio bat dau khong hop le.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/showtimes";
    }
}
