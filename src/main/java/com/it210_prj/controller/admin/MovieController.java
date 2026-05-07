package com.it210_prj.controller.admin;

import com.it210_prj.model.entity.Movie;
import com.it210_prj.service.admin.CategoryService;
import com.it210_prj.service.admin.GenreService;
import com.it210_prj.service.admin.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/admin/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;
    private final CategoryService categoryService;
    private final GenreService genreService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("movies", movieService.findAll());
        return "admin/movie/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("movie", new Movie());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("genres", genreService.findAll());
        return "admin/movie/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Movie movie) {
        movieService.save(movie);
        return "redirect:/admin/movies";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        movieService.deleteById(id);
        return "redirect:/admin/movies";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("movie", movieService.findById(id));
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("genres", genreService.findAll());
        return "admin/movie/form";
    }
}