package com.example.financetracker.service.impl;

import com.example.financetracker.entity.TelegramUserEntity;
import com.example.financetracker.entity.UserEntity;
import com.example.financetracker.repository.TelegramUserRepository;
import com.example.financetracker.repository.UserRepository;
import com.example.financetracker.service.TelegramUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class TelegramUserServiceImpl implements TelegramUserService {

    private final TelegramUserRepository telegramUserRepository;
    private final UserRepository userRepository;

    @Autowired
    public TelegramUserServiceImpl(TelegramUserRepository telegramUserRepository, UserRepository userRepository) {
        this.telegramUserRepository = telegramUserRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void saveJwtToken(Long telegram_id, String jwt_token, String refresh_token, String username) {
        log.info("Сохранение токенов для пользователя {}: token={}, refreshToken={}", telegram_id, jwt_token, refresh_token);;
        TelegramUserEntity telegramUser = telegramUserRepository.findByTelegramId(telegram_id);
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь с логином " + username + " не найден"));;
        if (telegramUser != null) {
            telegramUser.setTelegramId(telegram_id);
            telegramUser.setJwtToken(jwt_token);
            telegramUser.setRefreshToken(refresh_token);
            if (user != null) {telegramUser.setUsername(username);}
            telegramUserRepository.save(telegramUser);
            log.info("Токены успешно сохранены для существующего пользователя {}", telegram_id);
        }
        else telegramUser = new TelegramUserEntity();
        telegramUser.setTelegramId(telegram_id);
        telegramUser.setJwtToken(jwt_token);
        telegramUser.setRefreshToken(refresh_token);
        telegramUserRepository.save(telegramUser);
        log.info("Токены успешно сохранены для нового пользователя {}", telegram_id);
    }

    @Override
    public String getJwtToken(Long telegram_id) {
        TelegramUserEntity telegramUser = telegramUserRepository.findByTelegramId(telegram_id);
        if (telegramUser == null || telegramUser.getJwtToken() == null) {
            log.warn("Токен отсутствует для пользователя {}", telegram_id);
            throw new RuntimeException("Пользователь не авторизован. Пожалуйста, выполните вход.");
        }
        log.info("Получен токен для пользователя {}: {}", telegram_id, telegramUser.getJwtToken());
        return telegramUser.getJwtToken();
    }

    @Override
    public String getRefreshToken(Long telegram_id) {
        TelegramUserEntity telegramUser = telegramUserRepository.findByTelegramId(telegram_id);
        if (telegramUser == null || telegramUser.getRefreshToken() == null) {
            log.warn("Refresh-токен отсутствует для пользователя {}", telegram_id);
            return null;
        }
        log.info("Получен refresh-токен для пользователя {}: {}", telegram_id, telegramUser.getRefreshToken());
        return telegramUser.getRefreshToken();
    }

    @Override
    public void logout(Long telegram_id) {
        log.info("Выполняется выход для пользователя {}", telegram_id);
        TelegramUserEntity telegramUser = telegramUserRepository.findByTelegramId(telegram_id);
        if (telegramUser != null) {
            telegramUser.setJwtToken(null);
            telegramUser.setRefreshToken(null);
            telegramUserRepository.save(telegramUser);
            log.info("Выход успешно выполнен для пользователя {}", telegram_id);
        }
    }
}
