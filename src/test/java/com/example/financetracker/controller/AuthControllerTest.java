package com.example.financetracker.controller;

import com.example.financetracker.dto.*;
import com.example.financetracker.service.impl.AuthServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Slf4j
public class AuthControllerTest {

    @Mock
    private AuthServiceImpl authService;


    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createAuthToken_ShouldReturnJwtResponse_WhenCredentialsAreValid() {
        // Arrange
        JwtRequestDto authRequest = new JwtRequestDto("user", "password");
        JwtResponseDto jwtResponse = new JwtResponseDto("token");
        when(authService.createAuthToken(any(JwtRequestDto.class))).thenReturn(jwtResponse);

        // Act
        ResponseEntity<?> response = authController.createAuthToken(authRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(jwtResponse);
    }

    @Test
    void createNewUser_ShouldReturnUserDto_WhenRegistrationIsValid() {
        // Arrange
        RegistrationUserDto registrationUserDto = new RegistrationUserDto("testUser", "password", "password", "testEmail@gmail.com");

        Set<CategoryDto> expectedCategories = new HashSet<>();
        expectedCategories.add(new CategoryDto(3L, "Зарплата", "INCOME"));
        expectedCategories.add(new CategoryDto(2L, "Транспорт", "EXPENSE"));
        expectedCategories.add(new CategoryDto(1L, "Еда", "EXPENSE"));

        UserDto userDto = new UserDto(1L, "testUser", "testEmail@gmail.com", expectedCategories);

        when(authService.createNewUser(any(RegistrationUserDto.class))).thenReturn(userDto);

        // Act
        ResponseEntity<?> response = authController.createNewUser(registrationUserDto);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(userDto);
    }
}
