package com.example.financetracker.dto;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class ErrorResponseDto {
    private Integer status;
    private String message;
    private ZonedDateTime timestamp;

    public ErrorResponseDto(Integer status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = ZonedDateTime.now();
    }
}
