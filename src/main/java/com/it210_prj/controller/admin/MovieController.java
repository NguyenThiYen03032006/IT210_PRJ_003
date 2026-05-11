package com.it210_prj.controller.admin;

import com.it210_prj.model.entity.Category;
import com.it210_prj.model.entity.Genre;
import com.it210_prj.model.entity.Movie;
import com.it210_prj.service.admin.CategoryService;
import com.it210_prj.service.admin.GenreService;
import com.it210_prj.service.admin.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Quản trị phim MVC: form binding và normalize category/genre rỗng trước khi persist. */
@Controller
@RequestMapping("/admin/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;
    private final CategoryService categoryService;
    private final GenreService genreService;

    @GetMapping // Bỏ @RequestMapping("/admin/movies") thừa ở đây
    public String list(Model model) {
        model.addAttribute("movies", movieService.findAll());
        return "admin/movie/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        Movie movie = new Movie();
        // Khởi tạo sẵn object để tránh lỗi null khi binding field.id
        movie.setCategory(new Category());
        movie.setGenre(new Genre());

        model.addAttribute("movie", movie);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("genres", genreService.findAll());
        return "admin/movie/form";
    }

    /**
     * Model attribute có thể tạo Category/Genre với id null khi user chọn “trống” —
     * gán {@code null} cho association để Hibernate không flush proxy rỗng.
     */
    @PostMapping("/save")
    public String save(@ModelAttribute Movie movie) {
        // Nếu id của category/genre là null, hãy set cả object đó về null
        // để tránh lỗi Hibernate khi save object rỗng
        if (movie.getCategory() != null && movie.getCategory().getId() == null) {
            movie.setCategory(null);
        }
        if (movie.getGenre() != null && movie.getGenre().getId() == null) {
            movie.setGenre(null);
        }

        movieService.save(movie);
        return "redirect:/admin/movies";
    }

    // ... các hàm khác giữ nguyên
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            movieService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xoa phim thanh cong.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
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