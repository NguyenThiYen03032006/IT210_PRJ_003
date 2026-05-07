package com.it210_prj.controller;

import com.it210_prj.model.entity.UserProfile;
import com.it210_prj.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepo;

    @GetMapping("/profile")
    public UserProfile getProfile(Authentication auth) {
        String username = auth.getName();
        return userRepo.findByEmail(username).get().getProfile();
    }
}