package com.example.financetracker.controller;

import com.example.financetracker.dto.BudgetRequestDto;
import com.example.financetracker.dto.BudgetResponseDto;
import com.example.financetracker.entity.BudgetEntity;
import com.example.financetracker.entity.UserEntity;
import com.example.financetracker.service.impl.BudgetServiceImpl;
import com.example.financetracker.service.impl.UserServiceImpl;
import com.example.financetracker.mapper.BudgetMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/budgets")
@RequiredArgsConstructor
public class BudgetController {
    private final BudgetServiceImpl budgetService;
    private final BudgetMapper budgetMapper;
    private final UserServiceImpl userService;

    @Operation(summary = "Create a new budget", description = "Creates a new budget for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Budget created successfully",
                    content = @Content(schema = @Schema(implementation = BudgetResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @PostMapping
    public ResponseEntity<BudgetResponseDto> createBudget(
            @Valid @RequestBody BudgetRequestDto budgetRequest) {
        UserEntity user = userService.getCurrentUser();
        BudgetEntity budget = budgetService.createBudget(budgetRequest, user);
        BudgetResponseDto budgetResponse = budgetMapper.toDto(budget, budgetService);
        return new ResponseEntity<>(budgetResponse, HttpStatus.CREATED);
    }

    @Operation(summary = "Get user budgets", description = "Retrieves all budgets for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budgets retrieved successfully",
                    content = @Content(schema = @Schema(implementation = BudgetResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<List<BudgetResponseDto>> getUserBudgets() {
        UserEntity user = userService.getCurrentUser();
        List<BudgetEntity> budgets = budgetService.getUserBudgets(user);
        List<BudgetResponseDto> budgetResponses = budgets.stream()
                .map(budget -> budgetMapper.toDto(budget, budgetService))
                .toList();
        return ResponseEntity.ok(budgetResponses);
    }

    @Operation(summary = "Get budget by ID", description = "Retrieves a specific budget by its ID for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budget retrieved successfully",
                    content = @Content(schema = @Schema(implementation = BudgetResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - budget does not belong to user"),
            @ApiResponse(responseCode = "404", description = "Budget not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponseDto> getBudgetById(@PathVariable Long id) {
        UserEntity user = userService.getCurrentUser();
        BudgetEntity budget = budgetService.getBudgetById(id, user);
        BudgetResponseDto budgetResponse = budgetMapper.toDto(budget, budgetService);
        return ResponseEntity.ok(budgetResponse);
    }

    @Operation(summary = "Update a budget", description = "Updates an existing budget for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budget updated successfully",
                    content = @Content(schema = @Schema(implementation = BudgetResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - budget does not belong to user"),
            @ApiResponse(responseCode = "404", description = "Budget or category not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponseDto> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequestDto budgetRequest) {
        UserEntity user = userService.getCurrentUser();
        BudgetEntity updatedBudget = budgetService.updateBudget(id, budgetRequest, user);
        BudgetResponseDto budgetResponse = budgetMapper.toDto(updatedBudget, budgetService);
        return ResponseEntity.ok(budgetResponse);
    }

    @Operation(summary = "Delete a budget", description = "Deletes a specific budget for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Budget deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - budget does not belong to user"),
            @ApiResponse(responseCode = "404", description = "Budget not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        UserEntity user = userService.getCurrentUser();
        budgetService.deleteBudget(id, user);
        return ResponseEntity.noContent().build();
    }
}