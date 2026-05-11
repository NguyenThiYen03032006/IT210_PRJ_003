package com.it210_prj.repository;

import com.it210_prj.model.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByHotTrueOrderByIdAsc();

    @Query("""
            SELECT m FROM Movie m
            WHERE (:genreId IS NULL OR m.genre.id = :genreId)
            AND (
              :q IS NULL OR :q = ''
              OR LOWER(m.title) LIKE LOWER(CONCAT('%', :q, '%'))
              OR LOWER(COALESCE(m.description, '')) LIKE LOWER(CONCAT('%', :q, '%'))
              OR (m.genre IS NOT NULL AND LOWER(m.genre.name) LIKE LOWER(CONCAT('%', :q, '%')))
              OR (m.category IS NOT NULL AND LOWER(m.category.name) LIKE LOWER(CONCAT('%', :q, '%')))
            )
            ORDER BY m.title ASC
            """)
    List<Movie> searchCustomerMovies(@Param("genreId") Long genreId, @Param("q") String q);
}