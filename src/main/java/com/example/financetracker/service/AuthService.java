package com.example.financetracker.service;

import com.example.financetracker.dto.JwtRequestDto;
import com.example.financetracker.dto.JwtResponseDto;
import com.example.financetracker.dto.RegistrationUserDto;
import com.example.financetracker.dto.UserDto;

public interface AuthService {
    JwtResponseDto createAuthToken(JwtRequestDto authRequest);
    UserDto createNewUser(RegistrationUserDto registrationUserDto);
}
