package com.example.financetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyDynamicsDto(
        LocalDate date,
        String type,
        BigDecimal totalAmount
) {}
