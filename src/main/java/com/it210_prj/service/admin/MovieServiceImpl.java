package com.it210_prj.service.admin;


import com.it210_prj.model.entity.Movie;
import com.it210_prj.repository.MovieRepository;
import com.it210_prj.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/** CRUD phim và truy vấn hiển thị khách (lọc genre/tìm kiếm, phim hot). */
@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {
    private final MovieRepository movieRepository;
    private final ShowtimeRepository showtimeRepository;

    @Override
    public List<Movie> findAll() {
        return movieRepository.findAll();
    }

    /**
     * Chi tiết phim; không có id → ném lỗi để controller/UI báo.
     */
    @Override
    public Movie findById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
    }

    /**
     * Lưu hoặc cập nhật entity Movie (validation giao diện/controller).
     */
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

    /**
     * Danh sách phim công khai theo optional genreId và từ khóa tiêu đề (repository search).
     */
    @Override
    public List<Movie> findForCustomer(Long genreId, String q) {
        return movieRepository.searchCustomerMovies(genreId, q);
    }

    /**
     * Ưu tiên phim đánh dấu hot; nếu rỗng lấy tối đa 5 phim đầu trong DB.
     */
    @Override
    public List<Movie> findHotMovies() {
        List<Movie> hot = movieRepository.findByHotTrueOrderByIdAsc();
        if (!hot.isEmpty()) {
            return hot;
        }
        return movieRepository.findAll().stream().limit(5).toList();
    }
}
