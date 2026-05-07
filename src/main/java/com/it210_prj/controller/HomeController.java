package com.it210_prj.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/admin/home")
    public String adminHome() {
        return "admin/admin-home";
    }

    @GetMapping("/staff/home")
    public String staffHome() {
        return "staff/staff-home";
    }

    @GetMapping("/customer/home")
    public String customerHome() {
        return "customer/customer-home";
    }
}