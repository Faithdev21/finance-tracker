package com.example.financetracker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationUserDto {

    @NotBlank(message = "Username cannot be empty")
    private String username;

    private String password;
    private String confirmPassword;
    private String email;
}
