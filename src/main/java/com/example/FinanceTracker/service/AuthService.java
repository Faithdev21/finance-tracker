package com.example.FinanceTracker.service;

import com.example.FinanceTracker.dto.JwtRequest;
import com.example.FinanceTracker.dto.JwtResponse;
import com.example.FinanceTracker.dto.RegistrationUserDto;
import com.example.FinanceTracker.dto.UserDto;
import com.example.FinanceTracker.entity.CategoryEntity;
import com.example.FinanceTracker.entity.UserEntity;
import com.example.FinanceTracker.util.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashSet;
import java.util.List;


@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserServiceImpl userService;
    private final JwtTokenUtils jwtTokenUtils;
    private final AuthenticationManager authenticationManager;
    private final CategoryService categoryService;

    public JwtResponse createAuthToken(@RequestBody JwtRequest authRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        } catch (BadCredentialsException e) {
            throw new IllegalArgumentException("Incorrect login or password");
        }
        UserDetails userDetails = userService.loadUserByUsername(authRequest.getUsername());
        String token = jwtTokenUtils.generateToken(userDetails);
        return new JwtResponse(token);
    }

    public UserDto createNewUser(@RequestBody RegistrationUserDto registrationUserDto) {
        if (!registrationUserDto.getPassword().equals(registrationUserDto.getConfirmPassword())){
            throw new IllegalArgumentException("Passwords do not match");
        }
        if (userService.findByUsername(registrationUserDto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        UserEntity user = userService.createNewUser(registrationUserDto);
//        categoryService.createDefaultCategories();
        List<CategoryEntity> defaultCategories = categoryService.getDefaultCategories();
        user.setCategories(new HashSet<>(defaultCategories));
        userService.save(user);
        return new UserDto(user.getId(), user.getUsername(), user.getUsername());
    }
}
