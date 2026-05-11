package com.it210_prj.service;

import com.it210_prj.model.dto.RegisterRequest;
import com.it210_prj.model.entity.Role;
import com.it210_prj.model.entity.User;
import com.it210_prj.model.entity.UserProfile;
import com.it210_prj.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    public AuthServiceImpl(UserRepository userRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.encoder = encoder;
    }


    @Override
    public void register(RegisterRequest req) {
        if (userRepo.findByEmail(req.getEmail()).isPresent()) {
            throw new RuntimeException("Email da ton tai");
        }

        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new RuntimeException("Mat khau nhap lai khong khop");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(encoder.encode(req.getPassword()));
        user.setRole(Role.CUSTOMER);

        UserProfile profile = new UserProfile();
        profile.setFullName(req.getFullName());
        profile.setPhone(req.getPhone());
        profile.setUser(user);

        user.setProfile(profile);

        userRepo.save(user);
    }
}
