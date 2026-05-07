package com.it210_prj.service.admin;


import com.it210_prj.model.entity.Genre;

import java.util.List;

public interface GenreService {
    List<Genre> findAll();
    Genre findById(Long id);
    Genre save(Genre gener);
    void deleteById(Long id);
}
