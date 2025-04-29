package com.example.financetracker.service.impl;

import com.example.financetracker.dto.*;
import com.example.financetracker.entity.CategoryEntity;
import com.example.financetracker.entity.UserEntity;
import com.example.financetracker.service.AuthService;
import com.example.financetracker.util.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserServiceImpl userService;
    private final JwtTokenUtils jwtTokenUtils;
    private final AuthenticationManager authenticationManager;
    private final CategoryServiceImpl categoryService;

    public JwtResponseDto createAuthToken(@RequestBody JwtRequestDto authRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        } catch (BadCredentialsException e) {
            throw new IllegalArgumentException("Incorrect login or password");
        }
        UserDetails userDetails = userService.loadUserByUsername(authRequest.getUsername());
        String token = jwtTokenUtils.generateToken(userDetails);
        return new JwtResponseDto(token);
    }

    @Transactional
    public UserDto createNewUser(@RequestBody RegistrationUserDto registrationUserDto) {
        if (!registrationUserDto.getPassword().equals(registrationUserDto.getConfirmPassword())){
            throw new IllegalArgumentException("Passwords do not match");
        }
        if (registrationUserDto.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (userService.findByUsername(registrationUserDto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        UserEntity user = userService.createNewUser(registrationUserDto);
        List<CategoryEntity> defaultCategories = categoryService.getDefaultCategories();
        if (defaultCategories.isEmpty()) {
            log.warn("No default categories found in the database");
            throw new IllegalStateException("No default categories found in the database");
        }
        for (CategoryEntity category : defaultCategories) {
            user.addCategory(category);
        }
        log.info("user created {}", user.getId());
        UserDto userDto = new UserDto(user.getId(), user.getUsername(), user.getEmail());
        Set<CategoryDto> categoryDto = user.getCategories().stream()
                .map(CategoryDto::fromEntity)
                .collect(Collectors.toSet());
        userDto.setCategories(categoryDto);
        log.info("save");
        return userDto;
    }
}
