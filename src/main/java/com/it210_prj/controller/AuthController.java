package com.it210_prj.controller;

import com.it210_prj.model.dto.RegisterRequest;
import com.it210_prj.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
@Controller
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/register-page")
    public String registerPage(Model model) {
        model.addAttribute("req", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("req") RegisterRequest req,
            BindingResult result,
            Model model
    ) {

        // validate rỗng
        if (result.hasErrors()) {
            return "register";
        }

        try {
            authService.register(req);
            return "redirect:/auth/login";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
}