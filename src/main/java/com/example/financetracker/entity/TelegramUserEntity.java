package com.example.financetracker.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Table(name = "telegram_users")
@Getter
@Setter
public class TelegramUserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telegram_id", unique = true, nullable = false)
    private Long telegramId;

    @Column(name = "jwt_token")
    private String jwtToken;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "username")
    private String username;
}
