package com.example.financetracker.mapper;

import com.example.financetracker.dto.BudgetRequestDto;
import com.example.financetracker.dto.BudgetResponseDto;
import com.example.financetracker.entity.BudgetEntity;
import com.example.financetracker.service.impl.BudgetServiceImpl;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BudgetMapper {
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    BudgetResponseDto toDto(BudgetEntity budget, @Context BudgetServiceImpl budgetService);

    @Mapping(source = "categoryId", target = "category.id")
    BudgetEntity toEntity(BudgetRequestDto budgetRequest);
}