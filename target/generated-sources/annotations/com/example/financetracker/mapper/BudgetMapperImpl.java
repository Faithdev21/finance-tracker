package com.example.financetracker.mapper;

import com.example.financetracker.dto.BudgetRequestDto;
import com.example.financetracker.dto.BudgetResponseDto;
import com.example.financetracker.entity.BudgetEntity;
import com.example.financetracker.entity.CategoryEntity;
import com.example.financetracker.service.impl.BudgetServiceImpl;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-29T16:23:54+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Oracle Corporation)"
)
@Component
public class BudgetMapperImpl implements BudgetMapper {

    @Override
    public BudgetResponseDto toDto(BudgetEntity budget, BudgetServiceImpl budgetService) {
        if ( budget == null ) {
            return null;
        }

        BudgetResponseDto budgetResponseDto = new BudgetResponseDto();

        budgetResponseDto.setCategoryId( budgetCategoryId( budget ) );
        budgetResponseDto.setCategoryName( budgetCategoryName( budget ) );
        budgetResponseDto.setLimitAmount( budget.getLimitAmount() );
        budgetResponseDto.setPeriod( budget.getPeriod() );
        budgetResponseDto.setStartDate( budget.getStartDate() );
        budgetResponseDto.setEndDate( budget.getEndDate() );

        return budgetResponseDto;
    }

    @Override
    public BudgetEntity toEntity(BudgetRequestDto budgetRequest) {
        if ( budgetRequest == null ) {
            return null;
        }

        BudgetEntity.BudgetEntityBuilder budgetEntity = BudgetEntity.builder();

        budgetEntity.category( budgetRequestDtoToCategoryEntity( budgetRequest ) );
        budgetEntity.limitAmount( budgetRequest.getLimitAmount() );
        budgetEntity.period( budgetRequest.getPeriod() );
        budgetEntity.startDate( budgetRequest.getStartDate() );
        budgetEntity.endDate( budgetRequest.getEndDate() );

        return budgetEntity.build();
    }

    private Long budgetCategoryId(BudgetEntity budgetEntity) {
        if ( budgetEntity == null ) {
            return null;
        }
        CategoryEntity category = budgetEntity.getCategory();
        if ( category == null ) {
            return null;
        }
        Long id = category.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String budgetCategoryName(BudgetEntity budgetEntity) {
        if ( budgetEntity == null ) {
            return null;
        }
        CategoryEntity category = budgetEntity.getCategory();
        if ( category == null ) {
            return null;
        }
        String name = category.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    protected CategoryEntity budgetRequestDtoToCategoryEntity(BudgetRequestDto budgetRequestDto) {
        if ( budgetRequestDto == null ) {
            return null;
        }

        CategoryEntity.CategoryEntityBuilder categoryEntity = CategoryEntity.builder();

        categoryEntity.id( budgetRequestDto.getCategoryId() );

        return categoryEntity.build();
    }
}
