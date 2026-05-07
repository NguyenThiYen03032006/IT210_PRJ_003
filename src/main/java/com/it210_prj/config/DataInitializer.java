package com.it210_prj.config;

import com.it210_prj.model.entity.Role;
import com.it210_prj.model.entity.User;
import com.it210_prj.model.entity.UserProfile;
import com.it210_prj.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(UserRepository userRepo, PasswordEncoder encoder) {
        return args -> {

            if (userRepo.count() == 0) {

                // ===== ADMIN =====
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(encoder.encode("123456"));
                admin.setRole(Role.ADMIN);
                admin.setEmail("admin@gmail.com");

                UserProfile adminProfile = new UserProfile();
                adminProfile.setFullName("Admin System");
                adminProfile.setUser(admin);

                admin.setProfile(adminProfile);

                // ===== STAFF =====
                User staff = new User();
                staff.setUsername("staff");
                staff.setPassword(encoder.encode("123456"));
                staff.setRole(Role.STAFF);
                staff.setEmail("staff@gmail.com");

                UserProfile staffProfile = new UserProfile();
                staffProfile.setFullName("Staff User");
                staffProfile.setUser(staff);

                staff.setProfile(staffProfile);

                // ===== CUSTOMER =====
                User customer = new User();
                customer.setUsername("user");
                customer.setPassword(encoder.encode("123456"));
                customer.setRole(Role.CUSTOMER);
                customer.setEmail("user@gmail.com");


                UserProfile customerProfile = new UserProfile();
                customerProfile.setFullName("Nguyen Van A");
                customerProfile.setUser(customer);

                customer.setProfile(customerProfile);

                // lưu DB
                userRepo.save(admin);
                userRepo.save(staff);
                userRepo.save(customer);

                System.out.println("=== SEED DATA OK ===");
            }
        };
    }
}