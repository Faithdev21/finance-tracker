package com.example.financetracker.service;

import com.example.financetracker.dto.RegistrationUserDto;
import com.example.financetracker.dto.UserDto;
import com.example.financetracker.entity.RoleEntity;
import com.example.financetracker.entity.UserEntity;
import com.example.financetracker.repository.UserRepository;
import com.example.financetracker.service.impl.AuthServiceImpl;
import com.example.financetracker.service.impl.RoleServiceImpl;
import com.example.financetracker.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Incubating;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleServiceImpl roleService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private AuthServiceImpl authService;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void findByUsername_ShouldReturnUser_WhenUserExists() {
        // Arrange
        String username = "testUser";
        UserEntity user = new UserEntity();
        user.setUsername(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        Optional<UserEntity> result = userService.findByUsername(username);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo(username);
    }

    @Test
    void findByUsername_ShouldReturnEmpty_WhenUserDoesNotExist() {
        // Arrange
        String username = "nonExistentUser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act
        Optional<UserEntity> result = userService.findByUsername(username);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        // Arrange
        String username = "testUser";
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPassword("testPassword");
        RoleEntity role = new RoleEntity();
        role.setName("ROLE_USER");
        user.setRoles(List.of(role));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = userService.loadUserByUsername(username);

        // Assert
        assertThat(userDetails.getUsername()).isEqualTo(username);
        assertThat(userDetails.getPassword()).isEqualTo(user.getPassword());
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    void findById_ShouldReturnUser_WhenUserExists() {
        // Arrange
        Long id = 1L;
        UserEntity user = new UserEntity();
        user.setId(id);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        // Act
        Optional<UserEntity> result = userService.findById(id);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenUserDoesNotExist() {
        // Arrange
        Long id = 1L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        Optional<UserEntity> result = userService.findById(id);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void save_ShouldSaveUser_WhenUserIsValid() {
        // Arrange
        String username = "testUser";
        UserEntity user = new UserEntity();
        user.setUsername(username);
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);

        // Act
        UserEntity savedUser = userService.save(user);

        // Assert
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo(username);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void createNewUser_ShouldSaveUser_WhenRegistrationValid() {
        // Arrange
        RegistrationUserDto registrationUserDto = new RegistrationUserDto("testUser", "testPassword", "testPassword", "testEmail@gmail.com");
        RoleEntity role = new RoleEntity();
        role.setName("ROLE_USER");
        when(passwordEncoder.encode("testPassword")).thenReturn("encodedTestPassword");
        when(roleService.getUserRole()).thenReturn(role);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserEntity savedUser = userService.createNewUser(registrationUserDto);

        // Assert
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testUser");
        assertThat(savedUser.getPassword()).isEqualTo("encodedTestPassword");
        assertThat(savedUser.getEmail()).isEqualTo("testEmail@gmail.com");
        assertThat(savedUser.getRoles()).containsExactly(role);
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void createNewUser_ShouldNotSaveUser_WhenRegistrationInvalid() {
        // Arrange
        RegistrationUserDto registrationUserDto = new RegistrationUserDto("testUser", "testPassword", "wrongTestPassword", "testEmail@gmail.com");

        // Act
        UserDto savedUser = authService.createNewUser(registrationUserDto);

        // Assert
        assertThat(savedUser).isNull();
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void getCurrentUser_ShouldReturnUser_WhenUserIsAuthenticated() {
        String username = "testUser";
        UserEntity user = new UserEntity();
        user.setUsername(username);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsernameWithCategories(username)).thenReturn(Optional.of(user));

        // Act
        UserEntity result = userService.getCurrentUser();

        // Assert
        assertThat(result).isEqualTo(user);
        verify(userRepository, times(1)).findByUsernameWithCategories(username);
    }

    @Test
    void getCurrentUser_ShouldReturnUser_WhenUserIsNotAuthenticated() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act and Assert
        assertThatThrownBy(() -> userService.getCurrentUser())
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User is unauthenticated");
    }

    @Test
    void getCurrentUserDto_ShouldReturnUserDto_WhenUserIsAuthenticated() {
        // Arrange
        String username = "testUser";
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setId(1L);
        user.setEmail("testEmail@gmail.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsernameWithCategories(username)).thenReturn(Optional.of(user));

        // Act
        UserDto result = userService.getCurrentUserDto();

        // Assert
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getEmail()).isEqualTo("testEmail@gmail.com");
        verify(userRepository, times(1)).findByUsernameWithCategories(username);
    }
}
