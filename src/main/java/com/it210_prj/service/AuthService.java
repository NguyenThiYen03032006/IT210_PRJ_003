package com.it210_prj.service;

import com.it210_prj.model.dto.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest req);
}