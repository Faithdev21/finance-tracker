package com.example.FinanceTracker.service;

import com.example.FinanceTracker.entity.BudgetEntity;
import com.example.FinanceTracker.entity.NotificationEntity;
import com.example.FinanceTracker.entity.UserEntity;
import com.example.FinanceTracker.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private final NotificationRepository notificationRepository;

    @Transactional
    public void sendWarningNotification(BudgetEntity budget, BigDecimal currentSpending) {
        String message = String.format("Warning: You have spent %.2f of your %.2f budget for %s.",
                currentSpending, budget.getLimitAmount(), budget.getCategory().getName());

        logger.info("Warning notification for user {}: {}", budget.getUser().getId(), message);

        saveNotification(budget.getUser(), message);
    }

    @Transactional
    public void sendOverLimitNotification(BudgetEntity budget, BigDecimal currentSpending) {
        String message = String.format("Alert: You have exceeded your %.2f budget for %s. Current spending: %.2f.",
                budget.getLimitAmount(), budget.getCategory().getName(), currentSpending);

        logger.info("Over-limit notification for user {}: {}", budget.getUser().getId(), message);

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