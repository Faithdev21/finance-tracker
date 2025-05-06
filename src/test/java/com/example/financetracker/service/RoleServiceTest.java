package com.example.financetracker.service;

import com.example.financetracker.entity.RoleEntity;
import com.example.financetracker.exception.ResourceNotFoundException;
import com.example.financetracker.repository.RoleRepository;
import com.example.financetracker.service.impl.RoleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getUserRole_shouldReturnRole() {
        // Arrange
        RoleEntity role = new RoleEntity();
        role.setId(1);
        role.setName("ROLE_USER");

        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(role));

        // Act
        RoleEntity result = roleService.getUserRole();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("ROLE_USER");
        verify(roleRepository, times(1)).findByName("ROLE_USER");
    }

    @Test
    void getUserRole_shouldThrowException_WhenRoleNotExists() {
        // Arrange
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        // Act and Assert
        assertThatThrownBy(() -> roleService.getUserRole())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Role not found");
        verify(roleRepository, times(1)).findByName("ROLE_USER");
    }
}
