package com.it210_prj.service.admin;

import com.it210_prj.model.entity.Movie;

import java.util.List;

public interface MovieService {
    List<Movie> findAll();
    Movie findById(Long id);
    Movie save(Movie movie);
    void deleteById(Long id);

}
