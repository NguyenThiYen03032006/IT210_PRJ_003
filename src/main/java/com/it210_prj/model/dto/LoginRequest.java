package com.it210_prj.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class LoginRequest {

    @NotBlank(message = "Email khong duoc de trong")
    private String username;

    @NotBlank(message = "Mat khau khong duoc de trong")
    private String password;
}