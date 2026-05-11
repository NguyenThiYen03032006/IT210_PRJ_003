package com.it210_prj.service;

import com.it210_prj.model.dto.RegisterRequest;

public interface AuthService {

    /** Đăng ký khách (CUSTOMER); chi tiết kiểm tra trong lớp triển khai. */
    void register(RegisterRequest req);
}