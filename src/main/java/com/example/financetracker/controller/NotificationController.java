package com.example.financetracker.controller;

import com.example.financetracker.entity.NotificationEntity;
import com.example.financetracker.service.impl.NotificationServiceImpl;
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
    private final NotificationServiceImpl notificationService;

    @GetMapping
    public List<NotificationEntity> getUserNotifications(@RequestParam Long userId) {
        return notificationService.getUserNotifications(userId);
    }
}