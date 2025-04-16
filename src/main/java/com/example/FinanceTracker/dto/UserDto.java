package com.example.FinanceTracker.dto;

import com.example.FinanceTracker.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private Set<CategoryDto> categories;

    public UserDto(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }


    public static UserDto fromEntity(UserEntity entity) {
        return UserDto.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .categories(entity.getCategories().stream()
                        .map(CategoryDto::fromEntity)
                        .collect(Collectors.toSet()))
                .build();
    }
}

