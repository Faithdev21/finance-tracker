package com.example.financetracker.controller;


import com.example.financetracker.dto.JwtRequestDto;
import com.example.financetracker.dto.JwtResponseDto;
import com.example.financetracker.dto.RegistrationUserDto;
import com.example.financetracker.dto.UserDto;
import com.example.financetracker.service.impl.AuthServiceImpl;
import com.example.financetracker.util.JwtTokenUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthServiceImpl authService;
    private final JwtTokenUtils jwtTokenUtils;
    private final UserDetailsService userDetailsService;

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
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (jwtTokenUtils.validateRefreshToken(refreshToken)) {
            String username = jwtTokenUtils.getUsernameFromRefreshToken(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            String newJwtToken = jwtTokenUtils.generateToken(userDetails);
            Map<String, String> response = new HashMap<>();
            response.put("jwtToken", newJwtToken);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body("Недействительный refresh-токен");
        }
    }
}
