package com.it210_prj.service.admin;

import com.it210_prj.model.entity.Genre;
import com.it210_prj.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;



/** CRUD genre — logic mỏng, chủ yếu ủy quyền repository. */
@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService{
    private final GenreRepository genreRepository;


    @Override
    public List<Genre> findAll() {
        return genreRepository.findAll();
    }

    /**
     * Không có id → RuntimeException cho tầng gọi xử lý.
     */
    @Override
    public Genre findById(Long id) {
        return genreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Genre not found"));
    }

    @Override
    public Genre save(Genre gener) {
        return genreRepository.save(gener);
    }

    /**
     * Xóa cứng; không kiểm tra phim đang dùng genre (có thể lỗi FK tùy schema).
     */
    @Override
    public void deleteById(Long id) {
        genreRepository.deleteById(id);
    }
}
