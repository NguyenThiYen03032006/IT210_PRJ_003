package com.it210_prj.controller;

import com.it210_prj.model.dto.LoginRequest;
import com.it210_prj.model.entity.User;
import com.it210_prj.repository.UserRepository;
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

    public PageController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    // ===== LOGIN PAGE =====
    @GetMapping("/auth/login")
    public String loginPage(Model model) {
        // Đảm bảo key là "login" trùng với th:object trong HTML
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
            request.login(req.getUsername(), req.getPassword());
            return "redirect:/profile-page";

        } catch (Exception e) {
            // Lỗi sai tài khoản/mật khẩu
            model.addAttribute("loginError", "Tài khoản đang nhập không hợp lệ");
            return "login";
        }
    }
    // ===== USER CURRENT =====
    private User getCurrentUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()
                || auth.getName().equals("anonymousUser")) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        return userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
    }

    // ===== PROFILE =====
    @GetMapping("/profile-page")
    public String profile(Authentication auth, Model model) {

        User user = getCurrentUser(auth);

        String role = user.getRole().name();

        String homeUrl = "/customer/home";
        if (role.equals("ADMIN")) homeUrl = "/admin/home";
        else if (role.equals("STAFF")) homeUrl = "/staff/home";

        model.addAttribute("user", user);
        model.addAttribute("homeUrl", homeUrl);

        return "profile";
    }

    // ===== EDIT PROFILE =====
    @GetMapping("/edit-profile")
    public String editProfile(Authentication auth, Model model) {

        User user = getCurrentUser(auth);
        model.addAttribute("user", user);

        return "edit-profile";
    }

    // ===== UPDATE PROFILE =====
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