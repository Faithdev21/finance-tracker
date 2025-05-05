package com.example.financetracker.service;

import com.example.financetracker.dto.JwtRequestDto;
import com.example.financetracker.dto.JwtResponseDto;
import com.example.financetracker.dto.RegistrationUserDto;
import com.example.financetracker.entity.UserEntity;
import com.example.financetracker.service.impl.AuthServiceImpl;
import com.example.financetracker.service.impl.CategoryServiceImpl;
import com.example.financetracker.service.impl.UserServiceImpl;
import com.example.financetracker.util.JwtTokenUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class AuthServiceImplTest {

    @Mock
    private UserServiceImpl userService;

    @Mock
    private JwtTokenUtils jwtTokenUtils;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CategoryServiceImpl categoryService;


    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createAuthenticationToken_ShouldReturnJwtResponse_WhenCredentialsAreValid() {
        // Arrange
        JwtRequestDto authRequest = new JwtRequestDto("username", "password");
        UserDetails userDetails = new User("user", "password", Collections.emptyList());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userService.loadUserByUsername("username")).thenReturn(userDetails);
        when(jwtTokenUtils.generateToken(userDetails)).thenReturn("token");

        // Act
        JwtResponseDto response = authService.createAuthToken(authRequest);

        // Assert
        assertThat(response.getToken()).isEqualTo("token");
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService, times(1)).loadUserByUsername("username");
        verify(jwtTokenUtils, times(1)).generateToken(userDetails);
    }

    @Test
    void createAuthenticationToken_ShouldReturnJwtResponse_WhenCredentialsAreInvalid() {
        // Arrange
        JwtRequestDto authRequest = new JwtRequestDto("username", "wrongPassword");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(new BadCredentialsException("Bad Credentials"));

        // Act and Assert
        assertThatThrownBy(() -> authService.createAuthToken(authRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Incorrect login or password");

    }
    @Test
    void createNewUser_ShouldThrowException_WhenPasswordsDoNotMatch() {
        // Arrange
        RegistrationUserDto registrationUserDto = new RegistrationUserDto("testUser", "password", "differentPassword", "testEmail@gmail.com");

        // Act & Assert
        assertThatThrownBy(() -> authService.createNewUser(registrationUserDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Passwords do not match");
        verify(userService, never()).findByUsername(anyString());
        verify(userService, never()).createNewUser(any(RegistrationUserDto.class));
    }

    @Test
    void createNewUser_ShouldThrowException_WhenUsernameAlreadyExists() {
        // Arrange
        RegistrationUserDto registrationUserDto = new RegistrationUserDto("existedUser", "password", "password", "testEmail@gmail.com");
        UserEntity existingUser = new UserEntity();
        existingUser.setUsername("existedUser");
        when(userService.findByUsername("existedUser")).thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThatThrownBy(() -> authService.createNewUser(registrationUserDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username already exists");
        verify(userService, times(1)).findByUsername("existedUser");
        verify(userService, never()).createNewUser(any(RegistrationUserDto.class));
    }

    @Test
    void createNewUser_ShouldThrowException_WhenUsernameIsEmpty() {
        // Arrange
        RegistrationUserDto registrationUserDto = new RegistrationUserDto("", "testPassword", "testPassword", "testEmail@gmail.com");

        // Act & Assert
        assertThatThrownBy(() -> authService.createNewUser(registrationUserDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username cannot be empty");
    }
}
