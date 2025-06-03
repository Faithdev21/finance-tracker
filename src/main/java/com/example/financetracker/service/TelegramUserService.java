package com.example.financetracker.service;

public interface TelegramUserService {
    void saveJwtToken(Long telegram_id, String jwt_token, String refresh_token, String login);
    String getJwtToken(Long telegram_id);
    String getRefreshToken(Long telegram_id);
    void logout(Long telegram_id);
}
