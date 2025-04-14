package com.example.FinanceTracker.controller;

import com.example.FinanceTracker.dto.BalanceSummaryDto;
import com.example.FinanceTracker.dto.CategorySummaryDto;
import com.example.FinanceTracker.dto.DailyDynamics;
import com.example.FinanceTracker.dto.MonthlyStatisticDto;
import com.example.FinanceTracker.entity.UserEntity;
import com.example.FinanceTracker.service.TransactionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class AnalyticsController {
    private final TransactionServiceImpl transactionService;

    @GetMapping("/categorySummary")
    public ResponseEntity<List<CategorySummaryDto>> getCategorySummary(@RequestParam LocalDate startDate,
                                                                       @RequestParam LocalDate endDate,
                                                                       Principal principal) {
        UserEntity user = (UserEntity) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        return ResponseEntity.ok(transactionService.getCategorySummary(
                user.getId(),
                startDate.atStartOfDay(),
                endDate.atTime(LocalTime.MAX)
        ));
    }

    @GetMapping("/balanceSummary")
    public ResponseEntity<BalanceSummaryDto> getBalanceSummary(@RequestParam LocalDate startDate,
                                                                      @RequestParam LocalDate endDate,
                                                                      Principal principal) {
        UserEntity user = (UserEntity) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        return ResponseEntity.ok(transactionService.getBalanceSummary(
                user.getId(),
                startDate.atStartOfDay(),
                endDate.atTime(LocalTime.MAX)
        ));
    }

    @GetMapping("/dailyDynamics")
    public ResponseEntity<List<DailyDynamics>> getDailyDynamics(@RequestParam LocalDate startDate,
                                                                @RequestParam LocalDate endDate,
                                                                Principal principal) {
        UserEntity user = (UserEntity) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        return ResponseEntity.ok(transactionService.getDailyDynamics(
                user.getId(),
                startDate.atStartOfDay(),
                endDate.atTime(LocalTime.MAX)
        ));
    }

    @GetMapping("/monthlyStatistic")
    public ResponseEntity<List<MonthlyStatisticDto>> getMonthlyStatistics (@RequestParam LocalDate startDate,
                                                                           @RequestParam LocalDate endDate,
                                                                           Principal principal) {
        UserEntity user = (UserEntity) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        return ResponseEntity.ok(transactionService.getMonthlyStatistics(
                user.getId(),
                startDate.atStartOfDay(),
                endDate.atTime(LocalTime.MAX)
        ));
    }
}
