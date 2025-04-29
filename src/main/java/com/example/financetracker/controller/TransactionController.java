package com.example.financetracker.controller;

import com.example.financetracker.dto.PaginatedTransactionResponseDto;
import com.example.financetracker.dto.TransactionFilterRequestDto;
import com.example.financetracker.dto.TransactionRequestDto;
import com.example.financetracker.dto.TransactionResponseDto;
import com.example.financetracker.entity.TransactionEntity;
import com.example.financetracker.exception.ResourceNotFoundException;
import com.example.financetracker.service.impl.TransactionServiceImpl;
import com.example.financetracker.entity.UserEntity;
import com.example.financetracker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<TransactionResponseDto> createTransaction(
            @RequestBody @Valid TransactionRequestDto request,
            Authentication authentication
    ) {
        UserEntity user = getUserFromAuthentication(authentication);
        TransactionEntity transaction = transactionService.createTransaction(request, user);
//        return ResponseEntity.ok(transactionService.toResponse(transaction)); // Deprecated
        return new ResponseEntity<>(transactionService.toResponse(transaction), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<PaginatedTransactionResponseDto<TransactionResponseDto>> getUserTransactions(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            Authentication authentication
    ) {
        UserEntity user = getUserFromAuthentication(authentication);

        TransactionFilterRequestDto filterRequest = new TransactionFilterRequestDto(
                Optional.ofNullable(startDate),
                Optional.ofNullable(endDate),
                Optional.ofNullable(categoryId),
                page,
                size
        );

        Page<TransactionResponseDto> transactionPage = transactionService.getUserTransactions(user.getId(), filterRequest);

        PaginatedTransactionResponseDto<TransactionResponseDto> response = new PaginatedTransactionResponseDto<>(
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
