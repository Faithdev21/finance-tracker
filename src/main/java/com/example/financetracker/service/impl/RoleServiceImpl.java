package com.example.financetracker.service.impl;


import com.example.financetracker.entity.RoleEntity;
import com.example.financetracker.repository.RoleRepository;
import com.example.financetracker.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;

    public RoleEntity getUserRole() {
        return roleRepository.findByName("ROLE_USER").get();
    }
}
