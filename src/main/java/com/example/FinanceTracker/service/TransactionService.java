package com.example.FinanceTracker.service;

import com.example.FinanceTracker.dto.*;
import com.example.FinanceTracker.entity.TransactionEntity;
import com.example.FinanceTracker.entity.UserEntity;
import org.springframework.data.domain.Page;

public interface TransactionService {
    TransactionEntity createTransaction(TransactionRequest request, UserEntity user);
//    List<TransactionResponse> getUserTransactions(Long userId);
    TransactionResponse toResponse(TransactionEntity transaction);
    Page<TransactionResponse> getUserTransactions(Long userId, TransactionFilterRequest filter);
}