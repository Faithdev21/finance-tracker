package com.example.financetracker.telegram.util;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserStateContextUtil {
    private StatState state; // текущее состояние (start_date, end_date и т.д.)
    private LocalDate startDate;
    private LocalDate endDate;

    public void reset() {
        this.state = null;
        this.startDate = null;
        this.endDate = null;
    }
}

