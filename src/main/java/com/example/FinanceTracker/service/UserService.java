package com.example.FinanceTracker.service;

import com.example.FinanceTracker.entity.UserEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.Optional;


public interface UserService extends UserDetailsService {
    Optional<UserEntity> findByUsername(String username);
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
    Optional<UserEntity> findById(Long id);
    UserEntity save(UserEntity user);
}

