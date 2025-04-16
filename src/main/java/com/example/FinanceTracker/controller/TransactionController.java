package com.example.FinanceTracker.controller;

import com.example.FinanceTracker.dto.PaginatedTransactionResponse;
import com.example.FinanceTracker.dto.TransactionFilterRequest;
import com.example.FinanceTracker.dto.TransactionRequest;
import com.example.FinanceTracker.dto.TransactionResponse;
import com.example.FinanceTracker.entity.TransactionEntity;
import com.example.FinanceTracker.exception.ResourceNotFoundException;
import com.example.FinanceTracker.service.TransactionServiceImpl;
import com.example.FinanceTracker.entity.UserEntity;
import com.example.FinanceTracker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionServiceImpl transactionService;
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
    public ResponseEntity<PaginatedTransactionResponse<TransactionResponse>> getUserTransactions(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            Authentication authentication
    ) {
        UserEntity user = getUserFromAuthentication(authentication);

        TransactionFilterRequest filterRequest = new TransactionFilterRequest(
                Optional.ofNullable(startDate),
                Optional.ofNullable(endDate),
                Optional.ofNullable(categoryId),
                page,
                size
        );

        Page<TransactionResponse> transactionPage = transactionService.getUserTransactions(user.getId(), filterRequest);

        PaginatedTransactionResponse<TransactionResponse> response = new PaginatedTransactionResponse<>(
                transactionPage.getContent(),
                transactionPage.getNumber(),
                transactionPage.getTotalPages(),
                transactionPage.getTotalElements(),
                transactionPage.getSize()
        );

        return ResponseEntity.ok(response);
    }

    private UserEntity getUserFromAuthentication(Authentication authentication) {
        String username = authentication.getName();
        return userService.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

}
