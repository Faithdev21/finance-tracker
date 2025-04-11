package com.example.FinanceTracker.controller;


import com.example.FinanceTracker.dto.JwtRequest;
import com.example.FinanceTracker.dto.JwtResponse;
import com.example.FinanceTracker.dto.RegistrationUserDto;
import com.example.FinanceTracker.dto.UserDto;
import com.example.FinanceTracker.exception.AppError;
import com.example.FinanceTracker.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/auth")
    public ResponseEntity<?> createAuthToken(@RequestBody JwtRequest authRequest) {
        JwtResponse jwtResponse = authService.createAuthToken(authRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/registration")
    public ResponseEntity<?> createNewUser(@RequestBody RegistrationUserDto registrationUserDto) {
        UserDto userDto = authService.createNewUser(registrationUserDto);
        return ResponseEntity.ok(userDto);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<AppError> handleIllegalArgumentException(IllegalArgumentException e) {
        return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), e.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
