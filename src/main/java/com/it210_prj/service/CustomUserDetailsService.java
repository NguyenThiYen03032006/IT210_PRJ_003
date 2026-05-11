package com.it210_prj.service;

import com.it210_prj.model.entity.User;
import com.it210_prj.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

/**
 * Nạp user theo email cho Spring Security form login.
 * {@code authorities} dùng tên enum role (ADMIN/STAFF/CUSTOMER) để khớp {@code hasAuthority} trong SecurityConfig.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;

    public CustomUserDetailsService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    /** username thực tế là email trong hệ thống. */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user"));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRole().name())
                .build();
    }
}