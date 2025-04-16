package com.example.FinanceTracker.util;

import com.example.FinanceTracker.dto.BudgetRequestDto;
import com.example.FinanceTracker.dto.BudgetResponseDto;
import com.example.FinanceTracker.entity.BudgetEntity;
import com.example.FinanceTracker.service.BudgetService;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BudgetMapper {
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    BudgetResponseDto toDto(BudgetEntity budget, @Context BudgetService budgetService);

    @Mapping(source = "categoryId", target = "category.id")
    BudgetEntity toEntity(BudgetRequestDto budgetRequest);
}