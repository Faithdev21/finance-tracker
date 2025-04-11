package com.example.FinanceTracker.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@Table(name = "categories")
@NoArgsConstructor
@AllArgsConstructor
public class CategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, name = "name")
    private String name;

    @Column(name = "type")
    private String type;
}

