package com.example.FinanceTracker.controller;

import com.example.FinanceTracker.entity.NotificationEntity;
import com.example.FinanceTracker.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationEntity> getUserNotifications(@RequestParam Long userId) {
        return notificationService.getUserNotifications(userId);
    }
}