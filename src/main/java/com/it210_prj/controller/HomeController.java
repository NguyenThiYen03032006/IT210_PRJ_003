package com.it210_prj.controller;

import com.it210_prj.service.admin.GenreService;
import com.it210_prj.service.admin.MovieService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final MovieService movieService;
    private final GenreService genreService;

    public HomeController(MovieService movieService, GenreService genreService) {
        this.movieService = movieService;
        this.genreService = genreService;
    }

    @GetMapping("/admin/home")
    public String adminHome() {
        return "admin/admin-home";
    }

    @GetMapping("/staff/home")
    public String staffHome() {
        return "staff/staff-home";
    }

    @GetMapping("/customer/home")
    public String customerHome(Model model) {
        model.addAttribute("movies", movieService.findAll());
        model.addAttribute("genres", genreService.findAll());
        return "customer/customer-home";
    }
}
