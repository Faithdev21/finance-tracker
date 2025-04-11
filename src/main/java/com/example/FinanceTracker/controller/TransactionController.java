package com.example.FinanceTracker.controller;

import com.example.FinanceTracker.dto.TransactionRequest;
import com.example.FinanceTracker.dto.TransactionResponse;
import com.example.FinanceTracker.entity.TransactionEntity;
import com.example.FinanceTracker.exception.ResourceNotFoundException;
import com.example.FinanceTracker.service.TransactionService;
import com.example.FinanceTracker.entity.UserEntity;
import com.example.FinanceTracker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @RequestBody @Valid TransactionRequest request,
            Authentication authentication
    ) {
        UserEntity user = getUserFromAuthentication(authentication);
        TransactionEntity transaction = transactionService.createTransaction(request, user);
        return ResponseEntity.ok(transactionService.toResponse(transaction));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getUserTransactions(Authentication authentication) {
        UserEntity user = getUserFromAuthentication(authentication);
        return ResponseEntity.ok(transactionService.getUserTransactions(user.getId()));
    }

    private UserEntity getUserFromAuthentication(Authentication authentication) {
        String username = authentication.getName();
        return userService.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден: " + username));
    }
}
