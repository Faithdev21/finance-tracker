package com.example.financetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TransactionWithNotificationsResponseDto {
    private TransactionResponseDto transaction;
    private List<String> notifications;
}