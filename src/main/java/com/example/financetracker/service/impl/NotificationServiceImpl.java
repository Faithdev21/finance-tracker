package com.example.financetracker.service.impl;

import com.example.financetracker.entity.BudgetEntity;
import com.example.financetracker.entity.NotificationEntity;
import com.example.financetracker.entity.UserEntity;
import com.example.financetracker.repository.NotificationRepository;
import com.example.financetracker.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;

    @Transactional
    public void sendWarningNotification(BudgetEntity budget, BigDecimal currentSpending) {
        String message = String.format("Warning: You have spent %.2f of your %.2f budget for %s.",
                currentSpending, budget.getLimitAmount(), budget.getCategory().getName());

        log.info("Warning notification for user {}: {}", budget.getUser().getId(), message);

        saveNotification(budget.getUser(), message);
    }

    @Transactional
    public void sendOverLimitNotification(BudgetEntity budget, BigDecimal currentSpending) {
        String message = String.format("Alert: You have exceeded your %.2f budget for %s. Current spending: %.2f.",
                budget.getLimitAmount(), budget.getCategory().getName(), currentSpending);

        log.info("Over-limit notification for user {}: {}", budget.getUser().getId(), message);

        saveNotification(budget.getUser(), message);
    }

    private void saveNotification(UserEntity user, String message) {
        NotificationEntity notification = NotificationEntity.builder()
                .user(user)
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }

    public List<NotificationEntity> getUserNotifications(Long userId) {
        return notificationRepository.findByUserId(userId);
    }
}