package com.example.financetracker.controller;

import com.example.financetracker.dto.*;
import com.example.financetracker.entity.UserEntity;
import com.example.financetracker.service.impl.TransactionServiceImpl;
import com.example.financetracker.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class AnalyticsController {
    private final TransactionServiceImpl transactionService;
    private final UserServiceImpl userServiceImpl;

    @GetMapping("/categorySummary")
    public ResponseEntity<List<CategorySummaryDto>> getCategorySummary(@RequestParam LocalDate startDate,
                                                                       @RequestParam LocalDate endDate) {
        UserDto user = userServiceImpl.getCurrentUserDto();
        return ResponseEntity.ok(transactionService.getCategorySummary(
                user.getId(),
                startDate.atStartOfDay(),
                endDate.atTime(LocalTime.MAX)
        ));
    }

    @GetMapping("/balanceSummary")
    public ResponseEntity<BalanceSummaryDto> getBalanceSummary(@RequestParam LocalDate startDate,
                                                               @RequestParam LocalDate endDate) {
        UserDto user = userServiceImpl.getCurrentUserDto();
        return ResponseEntity.ok(transactionService.getBalanceSummary(
                user.getId(),
                startDate.atStartOfDay(),
                endDate.atTime(LocalTime.MAX)
        ));
    }

    @GetMapping("/dailyDynamics")
    public ResponseEntity<List<DailyDynamicsDto>> getDailyDynamics(@RequestParam LocalDate startDate,
                                                                   @RequestParam LocalDate endDate) {
        UserDto user = userServiceImpl.getCurrentUserDto();
        return ResponseEntity.ok(transactionService.getDailyDynamics(
                user.getId(),
                startDate.atStartOfDay(),
                endDate.atTime(LocalTime.MAX)
        ));
    }

    @GetMapping("/monthlyStatistic")
    public ResponseEntity<List<MonthlyStatisticDto>> getMonthlyStatistics (@RequestParam @DateTimeFormat(pattern = "yyyy-MM") String startDate,
                                                                           @RequestParam @DateTimeFormat(pattern = "yyyy-MM") String endDate) {
        UserDto user = userServiceImpl.getCurrentUserDto();
        LocalDateTime start = YearMonth.parse(startDate).atDay(1).atStartOfDay();
        LocalDateTime end = YearMonth.parse(endDate).atEndOfMonth().atTime(23, 59, 59);
        return ResponseEntity.ok(transactionService.getMonthlyStatistics(
                user.getId(),
                start,
                end
        ));
    }
}
