package com.example.financetracker.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponseDto {
    private String token;
    private String refreshToken;

    public JwtResponseDto(String token) {
        this.token = token;
        this.refreshToken = null;
    }
}
