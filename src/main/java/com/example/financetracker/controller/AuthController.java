package com.example.financetracker.controller;


import com.example.financetracker.dto.JwtRequestDto;
import com.example.financetracker.dto.JwtResponseDto;
import com.example.financetracker.dto.RegistrationUserDto;
import com.example.financetracker.dto.UserDto;
import com.example.financetracker.dto.ErrorResponseDto;
import com.example.financetracker.service.impl.AuthServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthServiceImpl authService;

    @PostMapping("/auth")
    public ResponseEntity<?> createAuthToken(@RequestBody JwtRequestDto authRequest) {
        JwtResponseDto jwtResponse = authService.createAuthToken(authRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/registration")
    public ResponseEntity<?> createNewUser(@RequestBody @Valid RegistrationUserDto registrationUserDto) {
        UserDto userDto = authService.createNewUser(registrationUserDto);
        return ResponseEntity.ok(userDto);
    }
}
