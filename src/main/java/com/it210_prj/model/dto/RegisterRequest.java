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
public class RegisterRequest {

    @NotBlank(message = "Email khong duoc de trong")
    private String email;

    @NotBlank(message = "Mat khau khong duoc de trong")
    private String password;

    @NotBlank(message = "Vui long nhap lai mat khau")
    private String confirmPassword;

    @NotBlank(message = "Username khong duoc de trong")
    private String username;

    @NotBlank(message = "Ho ten khong duoc de trong")
    private String fullName;

    private String phone;
}
