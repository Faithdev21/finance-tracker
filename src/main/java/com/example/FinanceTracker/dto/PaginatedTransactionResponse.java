package com.example.FinanceTracker.dto;

import java.util.List;

public record PaginatedTransactionResponse<T>(
        List<T> data,
        int currentPage,
        int totalPages,
        long totalItems,
        int itemsPerPage
) {}