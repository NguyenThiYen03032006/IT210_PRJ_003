package com.it210_prj.controller;

import com.it210_prj.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff/bookings")
@RequiredArgsConstructor
public class StaffBookingController {

    private final BookingService bookingService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("bookings", bookingService.getAllHistory());
        return "staff/booking-list";
    }

    @PostMapping("/{bookingId}/confirm")
    public String confirm(@PathVariable Long bookingId, RedirectAttributes redirectAttributes) {
        bookingService.confirmBooking(bookingId);
        redirectAttributes.addFlashAttribute("successMessage", "Da xac nhan ve #" + bookingId);
        return "redirect:/staff/bookings";
    }
}
