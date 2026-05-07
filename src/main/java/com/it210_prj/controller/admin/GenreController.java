package com.it210_prj.controller.admin;

import com.it210_prj.model.entity.Genre;
import com.it210_prj.service.admin.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("genres", genreService.findAll());
        return "admin/genre/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("genre", new Genre());
        return "admin/genre/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Genre genre) {
        genreService.save(genre);
        return "redirect:/admin/genres";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        genreService.deleteById(id);
        return "redirect:/admin/genres";
    }
}