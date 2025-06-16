package com.example.financetracker.telegram.handlers;

import com.example.financetracker.service.impl.TelegramUserServiceImpl;
import com.example.financetracker.telegram.TelegramBot;
import com.example.financetracker.telegram.util.CalendarInlineKeyboardUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@Slf4j
@Component
public class BudgetHandler {

    private final RestClient restClient;
    private final TelegramUserServiceImpl telegramUserService;
    private final CategoryHandler categoryHandler;
    static final String urlAPI = "http://localhost:8189/financeTracker";

    private final Map<Long, UserState> userStates = new HashMap<>();
    private final Map<Long, Map<String, String>> userData = new HashMap<>();
    private static final Set<String> COMMANDS = Set.of("/cancel", "/login", "/logout", "register", "/start");

    @Autowired
    public BudgetHandler(RestClient restClient, TelegramUserServiceImpl telegramUserService, CategoryHandler categoryHandler) {
        this.restClient = restClient;
        this.telegramUserService = telegramUserService;
        this.categoryHandler = categoryHandler;
    }

    private enum UserState {
        NONE,
        AWAITING_BUDGET_AMOUNT,
        AWAITING_BUDGET_PERIOD,
        AWAITING_BUDGET_START_DATE,
        AWAITING_BUDGET_END_DATE,
        AWAITING_BUDGET_START_DATE_CALENDAR,
        AWAITING_BUDGET_END_DATE_CALENDAR,
        AWAITING_BUDGET_AMOUNT_FOR_UPDATE,
        AWAITING_BUDGET_PERIOD_FOR_UPDATE,
        AWAITING_BUDGET_START_DATE_FOR_UPDATE,
        AWAITING_BUDGET_END_DATE_FOR_UPDATE
    }

    public void handleSetBudget(long chatId, TelegramBot bot) {
        if (!userStates.containsKey(chatId) || userStates.get(chatId) == UserState.NONE) {
            List<Map<String, Object>> categories = getUserCategories(chatId, bot);
            if (categories == null || categories.isEmpty()) {
                bot.sendMessage(chatId, "У вас нет категорий. Сначала добавьте категорию через меню Категории");
                return;
            }

            userStates.put(chatId, UserState.AWAITING_BUDGET_AMOUNT);
            userData.put(chatId, new HashMap<>());
            bot.sendMessageWithInlineKeyboard(chatId, "Выберите категорию для создания бюджета", createCategoryKeyboard(categories, "set_budget"));
        }
    }

    public void handleUpdateBudget(long chatId, TelegramBot bot) {
        if (!userStates.containsKey(chatId) || userStates.get(chatId) == UserState.NONE) {
            List<Map<String, Object>> budgets = getUserBudgets(chatId, bot);
            if (budgets == null || budgets.isEmpty()) {
                bot.sendMessage(chatId, "У вас нет бюджетов. Сначала создайте бюджет через меню Бюджет");
                return;
            }

            userStates.put(chatId, UserState.AWAITING_BUDGET_AMOUNT_FOR_UPDATE);
            userData.put(chatId, new HashMap<>());
            bot.sendMessageWithInlineKeyboard(chatId, "Выберите бюджет для обновления", createBudgetKeyboard(budgets, "update_budget"));
        }
    }

    public void handleDeleteBudget(long chatId, TelegramBot bot) {
        if (!userStates.containsKey(chatId) || userStates.get(chatId) == UserState.NONE) {
            List<Map<String, Object>> budgets = getUserBudgets(chatId, bot);
            if (budgets == null || budgets.isEmpty()) {
                bot.sendMessage(chatId, "У вас нет бюджетов. Сначала создайте бюджет через меню Бюджет");
                return;
            }

            bot.sendMessageWithInlineKeyboard(chatId, "Выберите бюджет для удаления", createBudgetKeyboard(budgets, "delete_budget"));
        }
    }

    private List<Map<String, Object>> getUserCategories(long chatId, TelegramBot bot) {
        try {
            String jwtToken = telegramUserService.getJwtToken(chatId);
            if (jwtToken == null) {
                bot.sendMessage(chatId, "Пожалуйста, выполните вход с помощью команды /login");
                return null;
            }

            String url = urlAPI + "/get_my_categories";
            String jsonResponse = restClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + jwtToken)
                    .retrieve()
                    .body(String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(jsonResponse, List.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                bot.refreshToken(chatId);
                bot.sendMessage(chatId, "Возникла ошибка. Повторите попытку.");
            } else {
                bot.sendMessage(chatId, "Ошибка при получении списка категорий: " + e.getMessage());
            }
            return null;
        } catch (Exception e) {
            bot.sendMessage(chatId, "Произошла ошибка: " + e.getMessage());
            return null;
        }
    }

    private List<Map<String, Object>> getUserBudgets(long chatId, TelegramBot bot) {
        try {
            String jwtToken = telegramUserService.getJwtToken(chatId);
            if (jwtToken == null) {
                bot.sendMessage(chatId, "Пожалуйста, выполните вход с помощью команды /login");
                return null;
            }

            String url = urlAPI + "/budgets";
            String jsonResponse = restClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + jwtToken)
                    .retrieve()
                    .body(String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(jsonResponse, List.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
//                bot.refreshToken(chatId);
                bot.sendMessage(chatId, "Токен истёк, пробуем обновить. Повторите попытку.");
            } else {
                bot.sendMessage(chatId, "Ошибка при получении списка бюджетов: " + e.getMessage());
            }
            return null;
        } catch (Exception e) {
            bot.sendMessage(chatId, "Произошла ошибка: " + e.getMessage());
            return null;
        }
    }

    private InlineKeyboardMarkup createCategoryKeyboard(List<Map<String, Object>> categories, String action) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (Map<String, Object> category : categories) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText((String) category.get("name"));
            button.setCallbackData(action + "_category_" + category.get("id"));
            keyboard.add(List.of(button));
        }
        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    private InlineKeyboardMarkup createBudgetKeyboard(List<Map<String, Object>> budgets, String action) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (Map<String, Object> budget : budgets) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            String categoryName = (String) budget.get("categoryName");
            String limitAmount = budget.get("limitAmount").toString();
            String period = (String) budget.get("period");
            button.setText(categoryName + " (" + limitAmount + ", " + period + ")");
            button.setCallbackData(action + "_budget_" + budget.get("id"));
            keyboard.add(List.of(button));
        }

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    public boolean isUserInBudgetState(long chatId) {
        UserState state = userStates.getOrDefault(chatId, UserState.NONE);
        return state != UserState.NONE;
    }

    public void resetUserState(long chatId) {
        userStates.remove(chatId);
        userData.remove(chatId);
    }

    public void handleCallbackQuery(long chatId, TelegramBot bot, String callbackData, int messageId) {
        if (callbackData.startsWith("set_budget_category_")) {
            String categoryId = callbackData.replace("set_budget_category_", "");
            userData.get(chatId).put("categoryId", categoryId);
            userStates.put(chatId, UserState.AWAITING_BUDGET_AMOUNT);
            bot.sendMessage(chatId, "Введите сумму бюджета (например 5000.00) или используйте /cancel для отмены");
        } else if (callbackData.startsWith("update_budget_budget_")) {
            String budgetId = callbackData.replace("update_budget_budget_", "");
            userData.get(chatId).put("budgetId", budgetId);
            userStates.put(chatId, UserState.AWAITING_BUDGET_AMOUNT_FOR_UPDATE);
            bot.sendMessage(chatId, "Введите новую сумму бюджета (например 1000.00) или используйте /cancel для отмены");
        } else if (callbackData.startsWith("delete_budget_budget_")) {
            String budgetId = callbackData.replace("delete_budget_budget_", "");
            handleDeleteBudgetConfirmed(chatId, bot, budgetId);
        } else if (callbackData.startsWith("budget:start_date:select:")) {
            String selectedDateStr = callbackData.replace("budget:start_date:select:", "");
            LocalDate date = LocalDate.parse(selectedDateStr);
            userData.get(chatId).put("startDate", date.atStartOfDay().toString());

            userStates.put(chatId, UserState.AWAITING_BUDGET_END_DATE_CALENDAR);
            bot.deleteMessage(chatId, messageId);
            bot.sendMessageWithInlineKeyboard(
                    chatId,
                    "📅 Выберите *дату конца* бюджета:",
                    CalendarInlineKeyboardUtil.generateDayKeyboard("budget", "end_date", date)
            );
        } else if (callbackData.startsWith("budget:end_date:select:")) {
            String selectedDateStr = callbackData.replace("budget:end_date:select:", "");
            LocalDate endDate = LocalDate.parse(selectedDateStr);

            String startDateStr = userData.get(chatId).get("startDate");
            LocalDate startDate = LocalDate.parse(startDateStr.substring(0, 10));

            if (endDate.isBefore(startDate)) {
                bot.sendMessage(chatId, "⚠️ Дата окончания не может быть раньше даты начала. Попробуйте снова.");
                return;
            }

            userData.get(chatId).put("endDate", endDate.atStartOfDay().toString());
            bot.deleteMessage(chatId, messageId);

            sendBudgetToApi(chatId, bot);
        } else if (callbackData.startsWith("budget:start_date:nav:")) {
            String newMonthStr = callbackData.replace("budget:start_date:nav:", "");
            LocalDate newDate = LocalDate.parse(newMonthStr);

            bot.deleteMessage(chatId, messageId);

            bot.sendMessageWithInlineKeyboard(
                    chatId,
                    "📅 Выберите *дату начала* бюджета:",
                    CalendarInlineKeyboardUtil.generateDayKeyboard("budget", "start_date", newDate)
            );
        } else if (callbackData.startsWith("budget:end_date:nav:")) {
            String newMonthStr = callbackData.replace("budget:end_date:nav:", "");
            LocalDate newDate = LocalDate.parse(newMonthStr);

            bot.deleteMessage(chatId, messageId);

            bot.sendMessageWithInlineKeyboard(
                    chatId,
                    "📅 Выберите *дату конца* бюджета:",
                    CalendarInlineKeyboardUtil.generateDayKeyboard("budget", "end_date", newDate)
            );
        }
    }

    private void handleDeleteBudgetConfirmed(long chatId, TelegramBot bot, String budgetId) {
        try {
            String jwtToken = telegramUserService.getJwtToken(chatId);
            if (jwtToken == null) {
                bot.sendMessage(chatId, "Пожалуйста, выполните вход с помощью команды /login.");
                return;
            }

            String url = urlAPI + "/budgets/" + budgetId;
            restClient.delete()
                    .uri(url)
                    .header("Authorization", "Bearer " + jwtToken)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
//                bot.refreshToken(chatId);
                bot.sendMessage(chatId, "Токен истёк, пробуем обновить. Повторите попытку.");
            } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                bot.sendMessage(chatId, "У вас нет прав для удаления этого бюджета.");
            } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                bot.sendMessage(chatId, "Бюджет не найден.");
            } else {
                bot.sendMessage(chatId, "Ошибка при удалении бюджета: " + e.getMessage());
            }
        } catch (Exception e) {
            bot.sendMessage(chatId, "Произошла ошибка: " + e.getMessage());
        } finally {
            resetUserState(chatId);
            bot.sendMainMenu(chatId);
        }
    }

    public void handleBudgetInput(long chatId, TelegramBot bot, String messageText) {
        log.info("Handling budget input for chatId: {}, message: {}", chatId, messageText);

        if (COMMANDS.contains(messageText.toLowerCase())) {
            return;
        }

        UserState state = userStates.get(chatId);
        Map<String, String> data = userData.get(chatId);

        switch (state) {
            case AWAITING_BUDGET_AMOUNT:
                handleBudgetAmountInput(chatId, bot, messageText, data, UserState.AWAITING_BUDGET_PERIOD);
                break;
            case AWAITING_BUDGET_PERIOD:
                handleBudgetPeriodInput(chatId, bot, messageText, data);
                break;
            case AWAITING_BUDGET_AMOUNT_FOR_UPDATE:
                handleBudgetAmountInput(chatId, bot, messageText, data, UserState.AWAITING_BUDGET_PERIOD_FOR_UPDATE);
                break;
            case AWAITING_BUDGET_PERIOD_FOR_UPDATE:
                handleBudgetPeriodInputForUpdate(chatId, bot, messageText, data);
                break;
            case AWAITING_BUDGET_START_DATE_FOR_UPDATE:
                handleBudgetStartDateInputForUpdate(chatId, bot, messageText, data);
                break;
            case AWAITING_BUDGET_END_DATE_FOR_UPDATE:
                handleBudgetEndDateInputForUpdate(chatId, bot, messageText, data);
                break;
            default:
                bot.sendMessage(chatId, "Неизвестное состояние. Используйте /cancel для отмены.");
        }
    }

    private void handleBudgetAmountInput(long chatId, TelegramBot bot, String messageText, Map<String, String> data, UserState nextState) {
        try {
            BigDecimal amount = new BigDecimal(messageText.trim());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                bot.sendMessage(chatId, "Бюджет должен превышать 0 рублей.");
                return;
            }
            data.put("limitAmount", amount.toString());
            userStates.put(chatId, nextState);
            log.info("Moving to next state: {}", nextState);
            bot.sendMessageWithKeyboard(chatId, "Пожалуйста, выберите период бюджета", createPeriodKeyboard());
        } catch (NumberFormatException e) {
            bot.sendMessage(chatId, "Пожалуйста, введите корректное число (например 5000.0)");
        }
    }

    private String changePeriodLanguageFromRussianToEnglish(String period) {
        return switch (period) {
            case "ГОД" -> "YEARLY";
            case "МЕСЯЦ" -> "MONTHLY";
            case "НЕДЕЛЯ" -> "WEEKLY";
            default -> period;
        };
    }
    private String changePeriodLanguageFromEnglishToRussian(String period) {
        return switch (period) {
            case "YEARLY" -> "ГОД";
            case "MONTHLY" -> "МЕСЯЦ";
            case "WEEKLY" -> "НЕДЕЛЯ";
            default -> period;
        };
    }
    private void handleBudgetPeriodInput(long chatId, TelegramBot bot, String messageText, Map<String, String> data) {
        String period = messageText.toUpperCase();
        if (!List.of("НЕДЕЛЯ","МЕСЯЦ", "ГОД").contains(period)) {
            bot.sendMessage(chatId, "Пожалуйста, выберите период, использую кнопки ниже");
            return;
        }
        period = changePeriodLanguageFromRussianToEnglish(period);
        data.put("period", period);
        userStates.put(chatId, UserState.AWAITING_BUDGET_START_DATE_CALENDAR);
        LocalDate today = LocalDate.now();
        bot.sendMessageWithInlineKeyboard(chatId, "📅 Выберите *дату начала* бюджета:",
                CalendarInlineKeyboardUtil.generateDayKeyboard("budget", "start_date", LocalDate.now()));
    }

    private void sendBudgetToApi(long chatId, TelegramBot bot) {
        Map<String, String> data = userData.get(chatId);

        try {
            String jwtToken = telegramUserService.getJwtToken(chatId);
            if (jwtToken == null) {
                bot.sendMessage(chatId, "Пожалуйста, выполните вход с помощью команды /login.");
                resetUserState(chatId);
                bot.sendMainMenu(chatId);
                return;
            }

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("categoryId", data.get("categoryId"));
            requestBody.put("limitAmount", data.get("limitAmount"));
            requestBody.put("period", data.get("period"));
            requestBody.put("startDate", data.get("startDate"));
            requestBody.put("endDate", data.get("endDate"));

            String url = urlAPI + "/budgets";
            restClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + jwtToken)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            bot.sendMessage(chatId, "✅ Бюджет успешно создан");
            resetUserState(chatId);
            bot.sendMainMenu(chatId);

        } catch (Exception e) {
            bot.sendMessage(chatId, "❌ Ошибка при создании: " + e.getMessage());
            resetUserState(chatId);
            bot.sendMainMenu(chatId);
        }
    }

    private ReplyKeyboardMarkup createPeriodKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("Неделя");
        row1.add("Месяц");
        keyboard.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add("Год");
        keyboard.add(row2);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        return keyboardMarkup;
    }

    private void handleBudgetPeriodInputForUpdate(long chatId, TelegramBot bot, String messageText, Map<String, String> data) {
        String period = messageText.toUpperCase();
        if (!List.of("НЕДЕЛЯ", "МЕСЯЦ", "ГОД").contains(period)) {
            bot.sendMessage(chatId, "Пожалуйста, выберите период, используя кнопки ниже.");
            return;
        }
        data.put("period", period);
        userStates.put(chatId, UserState.AWAITING_BUDGET_START_DATE_FOR_UPDATE);
        bot.sendMessage(chatId, "Введите новую дату начала бюджета в формате ГГГГ-ММ-ДД (например, 2025-06-15) или используйте /cancel для отмены");
    }

    private void handleBudgetStartDateInputForUpdate(long chatId, TelegramBot bot, String messageText, Map<String, String> data) {
        try {
            LocalDateTime startDate = LocalDateTime.parse(messageText.trim() + "T00:00:00");
            data.put("startDate", startDate.toString());
            userStates.put(chatId, UserState.AWAITING_BUDGET_END_DATE_FOR_UPDATE);
            bot.sendMessage(chatId, "Введите новую дату окончания бюджета в формате ГГГГ-ММ-ДД (например, 2025-12-08) или используйте /cancel для отмены");
        } catch (DateTimeParseException e) {
            bot.sendMessage(chatId, "Пожалуйста, введите дату в формате ГГГГ-ММ-ДД (например 2025-12-08) и попробуйте снова.");
        }
    }

    private void handleBudgetEndDateInputForUpdate(long chatId, TelegramBot bot, String messageText, Map<String, String> data) {
        try {
            LocalDateTime endDate = LocalDateTime.parse(messageText.trim() + "T00:00:00");
            LocalDateTime startDate = LocalDateTime.parse(data.get("startDate"));
            if (endDate.isBefore(startDate)) {
                bot.sendMessage(chatId, "Дата окончания не может быть раньше даты начала. Попробуйте снова.");
                return;
            }
            data.put("endDate", endDate.toString());

            String jwtToken = telegramUserService.getJwtToken(chatId);
            log.info("JWT TOKEN {}", jwtToken);
            if (jwtToken == null) {
                bot.sendMessage(chatId, "Пожалуйста, выполните вход с помощью команды /login.");
                resetUserState(chatId);
                bot.sendMainMenu(chatId);
                return;
            }

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("categoryId", data.get("categoryId"));
            requestBody.put("limitAmount", data.get("limitAmount"));
            requestBody.put("period", data.get("period"));
            requestBody.put("startDate", data.get("startDate"));
            requestBody.put("endDate", data.get("endDate"));

            String budgetId = data.get("budgetId");
            log.info("JWT TOKEN {}", jwtToken);
            String url = urlAPI + "/budgets/" + budgetId;
            Map<String, Object> response = restClient.put()
                    .uri(url)
                    .header("Authorization", "Bearer " + jwtToken)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);
        } catch (DateTimeParseException e) {
            bot.sendMessage(chatId, "Пожалуйста, введите дату в формате ГГГГ-ММ-ДД (например, 2025-12-08). Попробуйте снова.");
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
//                bot.refreshToken(chatId);
                bot.sendMessage(chatId, "Токен истёк, пробуем обновить. Повторите попытку.");
            } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                bot.sendMessage(chatId, "Бюджет или категория не найдены.");
            } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                bot.sendMessage(chatId, "У вас нет прав для обновления этого бюджета.");
            } else {
                bot.sendMessage(chatId, "Ошибка при обновлении бюджета: " + e.getMessage());
            }
            resetUserState(chatId);
            bot.sendMainMenu(chatId);
        } catch (Exception e) {
            bot.sendMessage(chatId, "Произошла ошибка: " + e.getMessage());
            resetUserState(chatId);
            bot.sendMainMenu(chatId);
        }
    }


    public void handleListBudgets(long chatId, TelegramBot bot) {
        try {
            String jwtToken = telegramUserService.getJwtToken(chatId);
            log.info("JWT TOKEN {}", jwtToken);
            if (jwtToken == null) {
                bot.sendMessage(chatId, "Пожалуйста, выполните вход с помощью команды /login.");
                return;
            }

            String url = urlAPI+"/budgets";
            String response = restClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + jwtToken)
                    .retrieve()
                    .body(String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String,Object>> budgets = objectMapper.readValue(response, List.class);

            if (budgets.isEmpty()) {
                bot.sendMessage(chatId, "У вас пока нет бюджетов. Создайте новый бюджет с помощью команды 'Установить бюджет'.");
                return;
            }

            StringBuilder message = new StringBuilder("Ваши бюджеты: \n");
            for (Map<String,Object> budget : budgets) {
                String categoryName = String.valueOf(budget.get("categoryName"));
                String limitAmount = String.valueOf(budget.get("limitAmount"));
                String period = (String) budget.get("period");
                period = changePeriodLanguageFromEnglishToRussian(period);

                String startDate = (String) budget.get("startDate");
                String endDate = (String) budget.get("endDate");
                String currentSpending = String.valueOf(budget.get("currentDate"));

                message.append(String.format("""
                                --------------
                                *Категория*: %s
                                *Лимит*: %s
                                *Период*: %s
                                *Начало*: %s
                                *Конец*: %s
                                *Текущие траты*: %s
                                --------------
                                
                                """,
                        categoryName, limitAmount, period, startDate, endDate, currentSpending));
            }


            bot.sendMessage(chatId, message.toString());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
//                bot.refreshToken(chatId);
                bot.sendMessage(chatId, "Токен истёк, пробуем обновить. Повторите попытку.");
            } else {
                bot.sendMessage(chatId, "Ошибка при получении списка бюджетов: " + e.getMessage());
            }
        } catch (Exception e) {
            bot.sendMessage(chatId, "Произошла ошибка: " + e.getMessage());
        }
    }
}