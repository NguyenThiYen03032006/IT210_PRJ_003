package com.it210_prj.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        //  Cho phép các tài nguyên tĩnh để giao diện không bị lỗi
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()

                        //  Cho phép vào trang login và các logic liên quan đến auth
                        .requestMatchers("/auth/**").permitAll()

                        .requestMatchers(HttpMethod.GET,
                                "/",
                                "/customer/home",
                                "/customer/movies/**",
                                "/customer/showtimes/*/seats"
                        ).permitAll()

                        //  Phân quyền
                        .requestMatchers("/admin/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN")

                        .requestMatchers("/staff/**").hasAnyAuthority("STAFF", "ROLE_STAFF")
                        .requestMatchers("/customer/**").hasAnyAuthority("CUSTOMER", "ROLE_CUSTOMER")

                        .requestMatchers("/profile-page", "/edit-profile", "/update-profile",
                                "/change-password").authenticated()// co trang thai dang nhap mơi đc truy cap trang

                        //  Mọi request khác đều phải login
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/internal-security-login")
                        .defaultSuccessUrl("/", true) // Chuyển hướng sau khi login thành công
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/auth/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                // tránh lỗi 404 khi không đủ quyền
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/auth/access-denied")
                );

        return http.build();
    }
}