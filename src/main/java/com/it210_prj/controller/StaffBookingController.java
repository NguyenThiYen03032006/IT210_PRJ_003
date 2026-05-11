package com.it210_prj.controller;

import com.it210_prj.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff/bookings")
@RequiredArgsConstructor
public class StaffBookingController {

    private final BookingService bookingService;

    @GetMapping
    public String list(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String q
    ) {
        Page<?> bookingPage = bookingService.getStaffHistory(q, page, size);
        model.addAttribute("bookingPage", bookingPage);
        model.addAttribute("bookings", bookingPage.getContent());
        model.addAttribute("q", q == null ? "" : q.trim());
        return "staff/booking-list";
    }

    @PostMapping("/{bookingId}/confirm")
    public String confirm(@PathVariable Long bookingId, RedirectAttributes redirectAttributes) {
        try {
            bookingService.confirmBooking(bookingId);
            redirectAttributes.addFlashAttribute("successMessage", "In ve thanh cong cho don #" + bookingId);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/staff/bookings";
    }

    @PostMapping("/{bookingId}/cancel")
    public String cancel(@PathVariable Long bookingId, RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBookingByStaff(bookingId);
            redirectAttributes.addFlashAttribute("successMessage", "Xac nhan huy va hoan tien thanh cong cho don #" + bookingId);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/staff/bookings";
    }
}
