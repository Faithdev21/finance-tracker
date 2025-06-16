package com.example.financetracker.telegram.handlers;

import com.example.financetracker.service.TelegramUserService;
import com.example.financetracker.telegram.TelegramBot;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.HttpStatus;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TransactionHandler {

    private final RestClient restClient;
    private final TelegramUserService telegramUserService;
    static final String urlAPI = "http://localhost:8189/financeTracker";
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<Long, UserState> userStates = new HashMap<>();

    private final Map<Long, Map<String, String>> userData = new HashMap<>();

    @Autowired
    public TransactionHandler(RestClient restClient, TelegramUserService telegramUserService) {
        this.restClient = restClient;
        this.telegramUserService = telegramUserService;
    }

    private enum UserState {
        NONE,
        AWAITING_AMOUNT,
        AWAITING_DESCRIPTION,
        AWAITING_CATEGORY,
        AWAITING_TYPE
    }

    public void handlePerformTransaction(long chatId, TelegramBot bot, String messageText) {

        if (!userStates.containsKey(chatId) || userStates.get(chatId) == UserState.NONE) {
            userStates.put(chatId, UserState.AWAITING_AMOUNT);
            userData.put(chatId, new HashMap<>());
            bot.sendMessage(chatId, "Введите сумму транзакции (например, 100.50) (или используйте /cancel для отмены):");
            return;
        }

        if (userStates.get(chatId) == UserState.AWAITING_AMOUNT) {
            try {
                double amount = Double.parseDouble(messageText.trim());
                if (amount <= 0) {
                    bot.sendMessage(chatId, "Сумма должна быть положительной. Попробуйте снова.");
                    return;
                }
                userData.get(chatId).put("amount", String.valueOf(amount));
                userStates.put(chatId, UserState.AWAITING_DESCRIPTION);
                bot.sendMessage(chatId, "Введите описание транзакции (или используйте /cancel для отмены):");
            } catch (NumberFormatException e) {
                bot.sendMessage(chatId, "Неверный формат суммы. Введите число, например, 100.50. Попробуйте снова.");
            }
            return;
        }

        if (userStates.get(chatId) == UserState.AWAITING_DESCRIPTION) {
            String description = messageText.trim();
            if (description.isEmpty()) {
                bot.sendMessage(chatId, "Описание не может быть пустым. Попробуйте снова.");
                return;
            }
            userData.get(chatId).put("description", description);
            userStates.put(chatId, UserState.AWAITING_CATEGORY);

            List<Map<String, Object>> categories = getUserCategories(chatId, bot);
            if (categories == null || categories.isEmpty()) {
                bot.sendMessage(chatId, "У вас нет категорий. Сначала добавьте категорию с помощью команды 'Добавить статью доходов/расходов'.");
                resetUserState(chatId);
                return;
            }

            bot.sendMessageWithKeyboard(chatId, "Выберите категорию для транзакции:", createCategoryKeyboard(categories));
            return;
        }

        if (userStates.get(chatId) == UserState.AWAITING_CATEGORY) {
            List<Map<String, Object>> categories = getUserCategories(chatId, bot);
            Map<String, Object> selectedCategory = categories.stream()
                    .filter(c -> c.get("name").equals(messageText))
                    .findFirst()
                    .orElse(null);

            if (selectedCategory == null) {
                bot.sendMessage(chatId, "Пожалуйста, выберите категорию из списка ниже.");
                return;
            }

            userData.get(chatId).put("categoryId", String.valueOf(selectedCategory.get("id")));
            userStates.put(chatId, UserState.AWAITING_TYPE);

            bot.sendMessageWithKeyboard(chatId, "Выберите тип транзакции:", createTransactionTypeKeyboard());
            return;
        }

        if (userStates.get(chatId) == UserState.AWAITING_TYPE) {
            String transactionType = messageText.equals("Доход") ? "INCOME" : messageText.equals("Расход") ? "EXPENSE" : null;
            if (transactionType == null) {
                bot.sendMessage(chatId, "Пожалуйста, выберите тип транзакции, используя кнопки ниже.");
                return;
            }

            String jwtToken = telegramUserService.getJwtToken(chatId);
            if (jwtToken == null) {
                bot.sendMessage(chatId, "Пожалуйста, выполните вход с помощью команды /login.");
                resetUserState(chatId);
                return;
            }

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("amount", Double.parseDouble(userData.get(chatId).get("amount")));
            requestBody.put("description", userData.get(chatId).get("description"));
            requestBody.put("categoryId", Long.parseLong(userData.get(chatId).get("categoryId")));

            try {
                String url = urlAPI + "/transactions";
                Map<String, Object> response = restClient.post()
                        .uri(url)
                        .header("Authorization", "Bearer " + jwtToken)
                        .body(requestBody)
                        .retrieve()
                        .body(Map.class);

                bot.sendMessage(chatId, "Транзакция успешно выполнена!\n" +
                        "Сумма: " + userData.get(chatId).get("amount") + "\n" +
                        "Описание: " + userData.get(chatId).get("description") + "\n" +
                        "Категория: " + messageText + "\n");

                Object notificationsObj = response.get("notifications");
                if (notificationsObj instanceof List<?> notificationsList) {
                    for (Object notification : notificationsList) {
                        if (notification instanceof String notificationMessage) {
                            bot.sendMessage(chatId, notificationMessage);
                        }
                    }
                }
                resetUserState(chatId);
                bot.sendMainMenu(chatId);
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    bot.refreshToken(chatId);
                    bot.sendMessage(chatId, "Токен истёк, пробуем обновить. Повторите попытку.");
                } else {
                    bot.sendMessage(chatId, "Ошибка при выполнении транзакции: " + e.getMessage());
                    resetUserState(chatId);
                    bot.sendMainMenu(chatId);
                }
            } catch (Exception e) {
                bot.sendMessage(chatId, "Произошла ошибка: " + e.getMessage());
                resetUserState(chatId);
                bot.sendMainMenu(chatId);
            }
        }
    }

    public void handleListTransactions(long chatId, TelegramBot bot) {
        try {
            String jwtToken = telegramUserService.getJwtToken(chatId);
            if (jwtToken == null) {
                bot.sendMessage(chatId, "Пожалуйста, выполните вход с помощью команды /login.");
                return;
            }

            String url = urlAPI + "/transactions";
            String jsonResponse = restClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + jwtToken)
                    .retrieve()
                    .body(String.class);

            Map<String, Object> responseMap = objectMapper.readValue(jsonResponse, Map.class);
            List<Map<String, Object>> transactions = (List<Map<String, Object>>) responseMap.get("data");

            if (transactions == null || transactions.isEmpty()) {
                bot.sendMessage(chatId, "У вас пока нет транзакций. Выполните новую транзакцию с помощью команды 'Выполнить транзакцию'.");
                return;
            }

            StringBuilder message = new StringBuilder("Ваши транзакции:\n");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            int index = 1;
            for (Map<String, Object> transaction : transactions) {
                Number amount = (Number) transaction.get("amount");
                String description = (String) transaction.get("description");
                String dateStr = (String) transaction.get("date");
                String categoryName = (String) transaction.get("categoryName");

                LocalDateTime date = LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME);

                message.append("\n - ")
                        .append("Сумма: ").append(String.format("%.2f", amount.doubleValue()))
                        .append(", Описание: ").append(description)
                        .append(", Дата: ").append(date.format(formatter))
                        .append(", Категория: ").append(categoryName)
                        .append("\n");
            }

            bot.sendMessage(chatId, message.toString());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                bot.refreshToken(chatId);
                bot.sendMessage(chatId, "Токен истёк, пробуем обновить. Повторите попытку.");
            } else {
                bot.sendMessage(chatId, "Ошибка при получении списка транзакций: " + e.getMessage());
            }
        } catch (Exception e) {
            bot.sendMessage(chatId, "Произошла ошибка: " + e.getMessage());
        }
    }

    public boolean isUserInTransactionState(long chatId) {
        UserState state = userStates.getOrDefault(chatId, UserState.NONE);
        return state != UserState.NONE;
    }

    public void resetUserState(long chatId) {
        userStates.remove(chatId);
        userData.remove(chatId);
    }

    private List<Map<String, Object>> getUserCategories(long chatId, TelegramBot bot) {
        try {
            String jwtToken = telegramUserService.getJwtToken(chatId);
            if (jwtToken == null) {
                return null;
            }

            String url = urlAPI + "/get_my_categories";
            String jsonResponse = restClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + jwtToken)
                    .retrieve()
                    .body(String.class);

            return objectMapper.readValue(jsonResponse, List.class);
        } catch (Exception e) {
            bot.sendMessage(chatId, "Ошибка при получении списка категорий: " + e.getMessage());
            return null;
        }
    }

    private ReplyKeyboardMarkup createCategoryKeyboard(List<Map<String, Object>> categories) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        for (Map<String, Object> category : categories) {
            KeyboardRow row = new KeyboardRow();
            row.add((String) category.get("name"));
            keyboard.add(row);
        }

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        return keyboardMarkup;
    }

    private ReplyKeyboardMarkup createTransactionTypeKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add("Доход");
        row.add("Расход");
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        return keyboardMarkup;
    }
}