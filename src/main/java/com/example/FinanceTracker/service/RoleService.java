package com.example.FinanceTracker.service;


import com.example.FinanceTracker.entity.RoleEntity;
import com.example.FinanceTracker.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleEntity getUserRole() {
        return roleRepository.findByName("ROLE_USER").get();
    }
}
