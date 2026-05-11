package com.it210_prj.controller;

import com.it210_prj.model.entity.Genre;
import com.it210_prj.service.admin.AdminDashboardService;
import com.it210_prj.service.admin.GenreService;
import com.it210_prj.service.admin.MovieService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private final MovieService movieService;
    private final GenreService genreService;
    private final AdminDashboardService adminDashboardService;

    public HomeController(
            MovieService movieService,
            GenreService genreService,
            AdminDashboardService adminDashboardService
    ) {
        this.movieService = movieService;
        this.genreService = genreService;
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/customer/home";
    }

    @GetMapping("/admin/home")
    public String adminHome(Model model) {
        model.addAttribute("dash", adminDashboardService.loadStats());
        return "admin/admin-home";
    }

    @GetMapping("/staff/home")
    public String staffHome() {
        return "redirect:/staff/bookings";
    }

    @GetMapping("/customer/home")
    public String customerHome(
            @RequestParam(required = false) Long genreId,
            @RequestParam(required = false) String q,
            Model model
    ) {
        String query = (q != null && !q.isBlank()) ? q.trim() : null;
        model.addAttribute("movies", movieService.findForCustomer(genreId, query));
        model.addAttribute("genres", genreService.findAll());
        model.addAttribute("featuredMovies", movieService.findHotMovies());
        model.addAttribute("selectedGenreId", genreId);
        model.addAttribute("searchQuery", query != null ? query : "");

        String selectedGenreName = null;
        if (genreId != null) {
            for (Genre g : genreService.findAll()) {
                if (genreId.equals(g.getId())) {
                    selectedGenreName = g.getName();
                    break;
                }
            }
        }
        model.addAttribute("selectedGenreName", selectedGenreName);
        model.addAttribute("hasActiveFilters", query != null || genreId != null);
        return "customer/customer-home";
    }
}
