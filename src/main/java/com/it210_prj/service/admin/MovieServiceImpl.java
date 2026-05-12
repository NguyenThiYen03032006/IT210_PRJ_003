package com.it210_prj.service.admin;


import com.it210_prj.model.entity.Movie;
import com.it210_prj.repository.MovieRepository;
import com.it210_prj.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {
    private final MovieRepository movieRepository;
    private final ShowtimeRepository showtimeRepository;

    @Override
    public List<Movie> findAll() {
        return movieRepository.findAll();
    }

    @Override
    public Movie findById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay phim."));
    }


    @Override
    public Movie save(Movie movie) {
        return movieRepository.save(movie);
    }

    @Override
    public void deleteById(Long id) {
        if (showtimeRepository.existsByMovieId(id)) {
            throw new RuntimeException("Khong the xoa phim vi dang co suat chieu.");
        }
        movieRepository.deleteById(id);
    }

    @Override
    public List<Movie> findForCustomer(Long genreId, String q) {
        return movieRepository.searchCustomerMovies(genreId, q);
    }


    @Override
    public List<Movie> findHotMovies() {
        List<Movie> hot = movieRepository.findByHotTrueOrderByIdAsc();
        if (!hot.isEmpty()) {
            return hot;
        }
        return movieRepository.findAll().stream().limit(5).toList();
    }
}
