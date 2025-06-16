package com.example.financetracker.service;

import com.example.financetracker.dto.*;
import com.example.financetracker.entity.TransactionEntity;
import com.example.financetracker.entity.UserEntity;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TransactionService {
    TransactionEntity createTransaction(TransactionRequestDto request, UserEntity user);
    TransactionResponseDto toResponse(TransactionEntity transaction);
    Page<TransactionResponseDto> getUserTransactions(Long userId, TransactionFilterRequestDto filter);
    List<String> getLastGeneratedNotifications();
}