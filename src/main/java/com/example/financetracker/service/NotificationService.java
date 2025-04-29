package com.example.financetracker.service;

import java.math.BigDecimal;
import java.util.List;

import com.example.financetracker.entity.BudgetEntity;
import com.example.financetracker.entity.NotificationEntity;
import org.springframework.transaction.annotation.Transactional;

public interface NotificationService {
    @Transactional
    void sendWarningNotification(BudgetEntity budget, BigDecimal currentSpending);

    @Transactional
    void sendOverLimitNotification(BudgetEntity budget, BigDecimal currentSpending);

    List<NotificationEntity> getUserNotifications(Long userId);
}