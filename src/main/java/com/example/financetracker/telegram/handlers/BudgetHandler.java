package com.example.financetracker.telegram.handlers;

import com.example.financetracker.service.TelegramUserService;
import com.example.financetracker.telegram.TelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class BudgetHandler {

    private final RestClient restClient;
    private final TelegramUserService telegramUserService;
    static final String urlAPI = "http://localhost:8189//financeTracker";

    @Autowired
    public BudgetHandler(RestClient restClient, TelegramUserService telegramUserService) {
        this.restClient = restClient;
        this.telegramUserService = telegramUserService;
    }

    public void handleSetBudget(long chatId, TelegramBot bot) {
        bot.sendMessage(chatId, "Функция установки бюджета пока не реализована. Введите данные бюджета в формате: сумма, категория, период.");
        // Здесь нужно добавить логику для ввода данных и вызова POST /budget
    }

    public void handleUpdateBudget(long chatId, TelegramBot bot) {
        bot.sendMessage(chatId, "Функция изменения бюджета пока не реализована. Введите ID бюджета и новые данные.");
        // Здесь нужно добавить логику для ввода данных и вызова PUT /budget/{id}
    }

    public void handleDeleteBudget(long chatId, TelegramBot bot) {
        bot.sendMessage(chatId, "Функция удаления бюджета пока не реализована. Введите ID бюджета.");
        // Здесь нужно добавить логику для ввода ID и вызова DELETE /budget/{id}
    }

    public void handleListBudgets(long chatId, TelegramBot bot) {
        try {
            String jwtToken = telegramUserService.getJwtToken(chatId);
            String url = urlAPI+"/api/budget";
            String response = restClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + jwtToken)
                    .retrieve()
                    .body(String.class);

            bot.sendMessage(chatId, "Список бюджетов: " + response); // Замени на реальные данные
        } catch (Exception e) {
            bot.sendMessage(chatId, "Ошибка при получении списка бюджетов: " + e.getMessage());
        }
    }
}