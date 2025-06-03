package com.example.financetracker.telegram.handlers;

import com.example.financetracker.service.TelegramUserService;
import com.example.financetracker.telegram.TelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class StatisticsHandler {

    private final RestClient restClient;
    private final TelegramUserService telegramUserService;

    static final String urlAPI = "http://localhost:8189//financeTracker";

    @Autowired
    public StatisticsHandler(RestClient restClient, TelegramUserService telegramUserService) {
        this.restClient = restClient;
        this.telegramUserService = telegramUserService;
    }

    public void handleMonthlyStatistics(long chatId, TelegramBot bot) {
        try {
            String jwtToken = telegramUserService.getJwtToken(chatId);
            String url = urlAPI+"/monthlyStatistic";
            String response = restClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + jwtToken)
                    .retrieve()
                    .body(String.class);

            bot.sendMessage(chatId, "Месячная статистика: " + response); // Здесь нужно добавить логику
        } catch (Exception e) {
            bot.sendMessage(chatId, "Ошибка при получении статистики: " + e.getMessage());
        }
    }

    public void handleDailyStatistics(long chatId, TelegramBot bot) {
        try {
            String jwtToken = telegramUserService.getJwtToken(chatId);
            String url = urlAPI+"/dailyDynamics";
            String response = restClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + jwtToken)
                    .retrieve()
                    .body(String.class);

            bot.sendMessage(chatId, "Ежедневная статистика: " + response); // Здесь нужно добавить логику
        } catch (Exception e) {
            bot.sendMessage(chatId, "Ошибка при получении статистики: " + e.getMessage());
        }
    }

    public void handleCategoryStatistics(long chatId, TelegramBot bot) {
        try {
            String jwtToken = telegramUserService.getJwtToken(chatId);
            String url = urlAPI+"/categorySummary";
            String response = restClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + jwtToken)
                    .retrieve()
                    .body(String.class);

            bot.sendMessage(chatId, "Статистика по категориям: " + response); // Здесь нужно добавить логику
        } catch (Exception e) {
            bot.sendMessage(chatId, "Ошибка при получении статистики: " + e.getMessage());
        }
    }

    public void handleBalanceSummary(long chatId, TelegramBot bot) {
        try {
            String jwtToken = telegramUserService.getJwtToken(chatId);
            String url = urlAPI+"/balanceSummary";
            String response = restClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + jwtToken)
                    .retrieve()
                    .body(String.class);

            bot.sendMessage(chatId, "Итоговая сумма: " + response); // Здесь нужно добавить логику
        } catch (Exception e) {
            bot.sendMessage(chatId, "Ошибка при получении итоговой суммы: " + e.getMessage());
        }
    }
}