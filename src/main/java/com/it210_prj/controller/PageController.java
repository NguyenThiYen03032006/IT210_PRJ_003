package com.it210_prj.controller;

import com.it210_prj.model.dto.LoginRequest;
import com.it210_prj.model.entity.User;
import com.it210_prj.repository.UserRepository;
import com.it210_prj.service.BookingService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class PageController {

    private final UserRepository userRepo;
    private final BookingService bookingService;

    public PageController(UserRepository userRepo, BookingService bookingService) {
        this.userRepo = userRepo;
        this.bookingService = bookingService;
    }

    @GetMapping("/auth/login")
    public String loginPage(Model model) {
        model.addAttribute("login", new LoginRequest());
        return "login";
    }

    @PostMapping("/auth/login")
    public String login(
            @Valid @ModelAttribute("login") LoginRequest req,
            BindingResult result,
            Model model,
            HttpServletRequest request
    ) {
        if (result.hasErrors()) {
            return "login";
        }

        try {
            // KIỂM TRA: Nếu đã đăng nhập rồi thì không login nữa
            if (request.getUserPrincipal() != null) {
                // Có thể thực hiện logout user cũ nếu muốn đăng nhập user mới:
                // request.logout();
            } else {
                // Chỉ gọi login nếu chưa có Principal (chưa đăng nhập)
                request.login(req.getUsername(), req.getPassword());
            }

            User user = userRepo.findByEmail(req.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String role = user.getRole().name();

            if ("ADMIN".equals(role)) {
                return "redirect:/admin/home";
            } else if ("STAFF".equals(role)) {
                return "redirect:/staff/home";
            } else {
                return "redirect:/customer/home";
            }

        } catch (ServletException e) {
            // Lỗi này xảy ra khi request.login thất bại hoặc "already authenticated"
            if (e.getMessage().contains("already authenticated")) {
                // Nếu đã đăng nhập rồi, redirect thẳng tới trang chủ tương ứng
                return "redirect:/profile-page";
            }
            model.addAttribute("loginError", "Tài khoản hoặc mật khẩu không chính xác");
            return "login";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("loginError", "Đã có lỗi hệ thống xảy ra");
            return "login";
        }
    }
    private User getCurrentUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()
                || auth.getName().equals("anonymousUser")) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        return userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
    }

    @GetMapping("/profile-page")
    public String profile(Authentication auth, Model model) {

        User user = getCurrentUser(auth);

        String role = user.getRole().name();

        String homeUrl = "/customer/home";
        if (role.equals("ADMIN")) homeUrl = "/admin/home";
        else if (role.equals("STAFF")) homeUrl = "/staff/home";

        model.addAttribute("user", user);
        model.addAttribute("homeUrl", homeUrl);
        if ("CUSTOMER".equals(role)) {
            model.addAttribute("bookings", bookingService.getHistory(user.getEmail()));
        }

        return "profile";
    }

    @GetMapping("/edit-profile")
    public String editProfile(Authentication auth, Model model) {

        User user = getCurrentUser(auth);
        model.addAttribute("user", user);

        return "edit-profile";
    }

    @PostMapping("/update-profile")
    public String updateProfile(Authentication auth,
                                @RequestParam String userName,
                                @RequestParam String fullName,
                                @RequestParam String email,
                                @RequestParam String phone,
                                Model model) {

        User user = getCurrentUser(auth);
        String currentEmail = user.getEmail();

        if (!email.equals(currentEmail) && userRepo.findByEmail(email).isPresent()) {
            model.addAttribute("user", user);
            model.addAttribute("error", "Email đã tồn tại!");
            return "edit-profile";
        }

        user.setUsername(userName);
        user.setEmail(email);
        user.getProfile().setFullName(fullName);
        user.getProfile().setPhone(phone);

        userRepo.save(user);

        if (!email.equals(currentEmail)) {
            return "redirect:/auth/login";
        }

        return "redirect:/profile-page";
    }
}
