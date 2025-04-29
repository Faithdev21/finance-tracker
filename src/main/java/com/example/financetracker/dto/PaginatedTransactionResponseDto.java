package com.example.financetracker.dto;

import java.util.List;

public record PaginatedTransactionResponseDto<T>(
        List<T> data,
        int currentPage,
        int totalPages,
        long totalItems,
        int itemsPerPage
) {}