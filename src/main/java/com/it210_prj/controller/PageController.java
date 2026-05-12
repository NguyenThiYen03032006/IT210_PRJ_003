package com.it210_prj.controller;

import com.it210_prj.model.dto.LoginRequest;
import com.it210_prj.model.entity.User;
import com.it210_prj.repository.UserRepository;
import com.it210_prj.service.BookingService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class PageController {

    private final UserRepository userRepo;
    private final BookingService bookingService;
    private final PasswordEncoder passwordEncoder;

    public PageController(
            UserRepository userRepo,
            BookingService bookingService,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepo = userRepo;
        this.bookingService = bookingService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/auth/login")
    public String loginPage(Model model) {
        model.addAttribute("login", new LoginRequest());
        return "login";
    }

    /** khi người dùng đã đăng nhập nhưng không đủ quyền truy cập URL. */
    @GetMapping("/auth/access-denied")
    public String accessDenied() {
        return "access-denied";
    }

   // lấy user hiện tại
    private User getCurrentUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()
                || auth.getName().equals("anonymousUser")) {
            throw new RuntimeException("Chua dang nhap.");
        }

        return userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User khong ton tai."));
    }

    @PostMapping("/auth/login")
    public String login(
            @Valid @ModelAttribute("login") LoginRequest req,
            BindingResult result,
            Model model,
            HttpServletRequest request
    ) {

//        if(result.hasErrors()){
//            return "login";
//        }

         User user= userRepo.findByEmail(req.getUsername()).orElse(null);

        // Kiểm tra tồn tại email nếu có nhập
        if (req.getUsername() != null && !req.getUsername().isBlank()) {
            user = userRepo.findByEmail(req.getUsername()).orElse(null);
        }

        boolean loginInvalid = false;

        // Nếu không tìm thấy user hoặc sai mật khẩu
        if (user == null ||
                (req.getPassword() != null &&
                        !req.getPassword().isBlank() &&
                        !passwordEncoder.matches(req.getPassword(), user.getPassword()))) {

            loginInvalid = true;
        }

        // Nếu có lỗi validate hoặc login sai
        if (result.hasErrors() || loginInvalid) {
            if (loginInvalid) {
                model.addAttribute("loginError",
                        "Tai khoan hoac mat khau khong chinh xac.");
            }
            return "login";
        }

        try {
            request.login(req.getUsername(), req.getPassword());
        } catch (ServletException e) {
            model.addAttribute("loginError", "Da co loi he thong.");
            return "login";
        }

        String role = user.getRole().name();

        if ("ADMIN".equals(role)) return "redirect:/admin/home";
        if ("STAFF".equals(role)) return "redirect:/staff/home";
        return "redirect:/customer/home";
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
            model.addAttribute("error", "Email da ton tai!");
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

    @GetMapping("/change-password")
    public String changePasswordPage() {
        return "change-password";
    }

    /** Kiểm tra khớp xác nhận, độ dài tối thiểu và mật khẩu hiện tại trước khi bcrypt mật khẩu mới. */
    @PostMapping("/change-password")
    public String changePassword(
            Authentication auth,
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Model model
    ) {
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Mat khau moi va xac nhan khong khop.");
            return "change-password";
        }
        if (newPassword.length() < 6) {
            model.addAttribute("error", "Mat khau moi can it nhat 6 ky tu.");
            return "change-password";
        }

        User user = getCurrentUser(auth);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            model.addAttribute("error", "Mat khau hien tai khong dung.");
            return "change-password";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
        return "redirect:/profile-page?pwd=ok";
    }
}
