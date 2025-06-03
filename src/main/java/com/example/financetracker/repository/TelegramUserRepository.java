package com.example.financetracker.repository;

import com.example.financetracker.entity.TelegramUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TelegramUserRepository extends JpaRepository<TelegramUserEntity, Long> {
    TelegramUserEntity findByTelegramId(Long telegramId);
}
