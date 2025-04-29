package com.example.financetracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransactionRequestDto(
        @NotNull @Positive BigDecimal amount,
        @NotBlank String description,
        @NotNull Long categoryId
) {}
