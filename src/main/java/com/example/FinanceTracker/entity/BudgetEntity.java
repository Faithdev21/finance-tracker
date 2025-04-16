package com.example.FinanceTracker.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@Table(name = "budgets")
@NoArgsConstructor
@AllArgsConstructor
public class BudgetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne()
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;


    @Column(nullable = false)
    private BigDecimal limitAmount;

    @Enumerated(EnumType.STRING)
    @Column
    private BudgetPeriodEntity period;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;
}
