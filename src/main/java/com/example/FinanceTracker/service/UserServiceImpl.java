package com.example.FinanceTracker.service;

import com.example.FinanceTracker.dto.RegistrationUserDto;
import com.example.FinanceTracker.dto.UserDto;
import com.example.FinanceTracker.entity.UserEntity;
import com.example.FinanceTracker.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    @Override
    public Optional<UserEntity> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(String.format("User %s not found", username)));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList())
        );
    }

    @Override
    public Optional<UserEntity> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public UserEntity save(UserEntity user) {
        return userRepository.save(user);
    }

    public UserEntity createNewUser(RegistrationUserDto registrationUserDto) {
        UserEntity user = new UserEntity();
        user.setUsername(registrationUserDto.getUsername());
        user.setEmail(registrationUserDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationUserDto.getPassword()));
        user.setRoles(List.of(roleService.getUserRole()));
        return userRepository.save(user);
    }

    public UserEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User id unauthenticated");
        }

        String username = authentication.getName();

         UserEntity user = userRepository.findByUsernameWithCategories(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with username " + username + " not found"));
        log.info("Loaded user: {} with categories: {}", user.getUsername(), user.getCategories());
        return user;
    }

    @Transactional
    public UserDto getCurrentUserDto() {
        UserEntity user = getCurrentUser();
        return UserDto.fromEntity(user);
    }
}
