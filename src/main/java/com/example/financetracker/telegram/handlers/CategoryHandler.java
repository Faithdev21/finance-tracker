package com.example.financetracker.telegram.handlers;

import com.example.financetracker.service.TelegramUserService;
import com.example.financetracker.telegram.TelegramBot;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.*;

@Component
public class CategoryHandler {

    private final RestClient restClient;
    private final TelegramUserService telegramUserService;
    static final String urlAPI = "http://localhost:8189/financeTracker";

    private final Map<Long, UserState> userStates = new HashMap<>();

    private static final Set<String> COMMANDS = Set.of("/cancel", "/login", "/logout", "/register", "/start");

    @Autowired
    public CategoryHandler(RestClient restClient, TelegramUserService telegramUserService) {
        this.restClient = restClient;
        this.telegramUserService = telegramUserService;
    }

    private enum UserState {
        NONE,
        AWAITING_CATEGORY_NAME,
        AWAITING_CATEGORY_TYPE
    }

    private final Map<Long, Map<String, String>> userData = new HashMap<>();

    public void handleAddCategory(long chatId, TelegramBot bot, String messageText) {
        if (COMMANDS.contains(messageText.toLowerCase())) {
            return;
        }

        if (!userStates.containsKey(chatId) || userStates.get(chatId) == UserState.NONE) {
            userStates.put(chatId, UserState.AWAITING_CATEGORY_NAME);
            userData.put(chatId, new HashMap<>());
            bot.sendMessage(chatId, "Введите название категории (или используйте /cancel для отмены):");
            return;
        }

        if (userStates.get(chatId) == UserState.AWAITING_CATEGORY_NAME) {
            String categoryName = messageText.trim();
            if (categoryName.isEmpty()) {
                bot.sendMessage(chatId, "Название категории не может быть пустым. Попробуйте снова.");
                return;
            }

            userData.get(chatId).put("name", categoryName);
            userStates.put(chatId, UserState.AWAITING_CATEGORY_TYPE);

            bot.sendMessageWithKeyboard(chatId, "Выберите тип категории:", createCategoryTypeKeyboard());
            return;
        }

        if (userStates.get(chatId) == UserState.AWAITING_CATEGORY_TYPE) {
            String categoryType = messageText.equals("Доход") ? "INCOME" : messageText.equals("Расход") ? "EXPENSE" : null;
            if (categoryType == null) {
                bot.sendMessage(chatId, "Пожалуйста, выберите тип категории, используя кнопки ниже.");
                return;
            }

            userData.get(chatId).put("type", categoryType);

            String jwtToken = telegramUserService.getJwtToken(chatId);
            if (jwtToken == null) {
                bot.sendMessage(chatId, "Пожалуйста, выполните вход с помощью команды /login.");
                resetUserState(chatId);
                bot.sendMainMenu(chatId);
                return;
            }

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("name", userData.get(chatId).get("name"));
            requestBody.put("type", categoryType);

            try {
                // Отправляем запрос на API
                String url = urlAPI + "/category_to_current_user";
                Map<String, Object> response = restClient.post()
                        .uri(url)
                        .header("Authorization", "Bearer " + jwtToken)
                        .body(requestBody)
                        .retrieve()
                        .body(Map.class);

                bot.sendMessage(chatId, "Категория '" + requestBody.get("name") + "' успешно добавлена!");
                resetUserState(chatId);
                bot.sendMainMenu(chatId);
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    bot.refreshToken(chatId); // Пробуем обновить токен
                    bot.sendMessage(chatId, "Токен истёк, пробуем обновить. Повторите попытку.");
                } else {
                    bot.sendMessage(chatId, "Ошибка при добавлении категории: " + e.getMessage());
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

    public void handleListCategories(long chatId, TelegramBot bot) {
        try {
            String jwtToken = telegramUserService.getJwtToken(chatId);
            if (jwtToken == null) {
                bot.sendMessage(chatId, "Пожалуйста, выполните вход с помощью команды /login.");
                return;
            }

            String url = urlAPI + "/get_my_categories";

            String jsonResponse = restClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + jwtToken)
                    .retrieve()
                    .body(String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, Object>> categories = objectMapper.readValue(jsonResponse, List.class);

            if (categories.isEmpty()) {
                bot.sendMessage(chatId, "У вас пока нет категорий. Добавьте новую категорию с помощью команды 'Добавить статью доходов/расходов'.");
                return;
            }

            StringBuilder message = new StringBuilder("Ваши категории:\n");
            for (Map<String, Object> category : categories) {
                String name = (String) category.get("name");
                message.append("- ").append(name).append("\n");
            }

            bot.sendMessage(chatId, message.toString());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                bot.refreshToken(chatId);
                bot.sendMessage(chatId, "Токен истёк, пробуем обновить. Повторите попытку.");
            } else {
                bot.sendMessage(chatId, "Ошибка при получении списка категорий: " + e.getMessage());
            }
        } catch (Exception e) {
            bot.sendMessage(chatId, "Произошла ошибка: " + e.getMessage());
        }
    }

    public boolean isUserInCategoryAddingState(long chatId) {
        UserState state = userStates.getOrDefault(chatId, UserState.NONE);
        return state == UserState.AWAITING_CATEGORY_NAME || state == UserState.AWAITING_CATEGORY_TYPE;
    }

    public void resetUserState(long chatId) {
        userStates.remove(chatId);
        userData.remove(chatId);
    }

    private ReplyKeyboardMarkup createCategoryTypeKeyboard() {
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