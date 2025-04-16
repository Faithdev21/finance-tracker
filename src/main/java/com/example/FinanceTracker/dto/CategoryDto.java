package com.example.FinanceTracker.dto;

import com.example.FinanceTracker.entity.CategoryEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDto {
    private Long id;
    private String name;
    private String type;

    public static CategoryDto fromEntity(CategoryEntity entity) {
        return CategoryDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .type(entity.getType())
                .build();
    }
}
