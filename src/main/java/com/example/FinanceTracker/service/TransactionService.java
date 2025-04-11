package com.example.FinanceTracker.service;

import com.example.FinanceTracker.dto.TransactionRequest;
import com.example.FinanceTracker.dto.TransactionResponse;
import com.example.FinanceTracker.entity.CategoryEntity;
import com.example.FinanceTracker.entity.TransactionEntity;
import com.example.FinanceTracker.repository.TransactionRepository;
import com.example.FinanceTracker.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;

    public TransactionEntity createTransaction(TransactionRequest transactionRequest, UserEntity user) {
        CategoryEntity category = categoryService.getCategoryByIdAndUser(transactionRequest.categoryId(), user);

        TransactionEntity transaction = new TransactionEntity();
        transaction.setAmount(transactionRequest.amount());
        transaction.setDescription(transactionRequest.description());
        transaction.setDate(LocalDateTime.now());
        transaction.setCategory(category);
        transaction.setUser(user);

        return transactionRepository.save(transaction);
    }

    public List<TransactionResponse> getUserTransactions(Long userId) {
        List<TransactionEntity> transactions = transactionRepository.findByUserId(userId);

        return transactions
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TransactionResponse toResponse(TransactionEntity transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getDate(),
                transaction.getCategory().getName()
        );
    }
}
