package com.example.FinanceTracker.user.service;


import com.example.FinanceTracker.user.entities.Role;
import com.example.FinanceTracker.user.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    public Role getUserRole() {
        return roleRepository.findByName("ROLE_USER").get();
    }
}
