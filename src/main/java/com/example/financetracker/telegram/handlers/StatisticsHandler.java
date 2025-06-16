package com.example.financetracker.telegram.handlers;

import com.example.financetracker.service.TelegramUserService;
import com.example.financetracker.telegram.TelegramBot;
import com.example.financetracker.telegram.util.CalendarInlineKeyboardUtil;
import com.example.financetracker.telegram.util.StatState;
import com.example.financetracker.telegram.util.UserStateContextUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.financetracker.telegram.util.StatState.AWAITING_MONTHLY_END;
import static com.example.financetracker.telegram.util.StatState.AWAITING_MONTHLY_START;

@Component
@Slf4j
public class StatisticsHandler {

    private final Map<Long, UserStateContextUtil> userContexts = new HashMap<>();

    private final TelegramUserService telegramUserService;
    private final RestClient restClient;
    private final String urlAPI = "http://localhost:8189/financeTracker";

    @Autowired
    public StatisticsHandler(TelegramUserService telegramUserService, RestClient restClient) {
        this.telegramUserService = telegramUserService;
        this.restClient = restClient;
    }

    public void startMonthlyStatFlow(long chatId, TelegramBot bot) {
        UserStateContextUtil context = new UserStateContextUtil();
        context.setState(AWAITING_MONTHLY_START);
        userContexts.put(chatId, context);

        YearMonth current = YearMonth.now();
        InlineKeyboardMarkup calendar = CalendarInlineKeyboardUtil.generateMonthKeyboard("monthly_start", current);
        bot.sendMessageWithInlineKeyboard(chatId, "Выберите начальный месяц", calendar);
    }

    public void startDailyStatFlow(long chatId, TelegramBot bot) {
        UserStateContextUtil context = new UserStateContextUtil();
        context.setState(StatState.AWAITING_DAILY_START);
        userContexts.put(chatId, context);

        LocalDate now = LocalDate.now();
        InlineKeyboardMarkup calendar = CalendarInlineKeyboardUtil.generateDayKeyboard("statistics", "daily_start", now);
        bot.sendMessageWithInlineKeyboard(chatId, "Выберите начальную дату", calendar);
    }

    public boolean isInStatFlow(Long chatId) {
        return userContexts.containsKey(chatId);
    }

    public void cancelStatFlow(Long chatId) {
        userContexts.remove(chatId);
    }

    public void handleCallback(long chatId, String data, TelegramBot bot, int messageId) {
        if (!data.startsWith("statistics:")) return;

        String[] parts = data.split(":");
        if (parts.length < 4) return;

        String section = parts[1];
        String action = parts[2];
        String value = parts[3];

        UserStateContextUtil context = userContexts.get(chatId);
        if (context == null) {
            bot.sendMessage(chatId, "Сессия статистики не активна. Используйте меню.");
            return;
        }

        try {
            switch (context.getState()) {
                case AWAITING_MONTHLY_START:
                    if (action.equals("select")) {
                        YearMonth ym = YearMonth.parse(value);
                        context.setStartDate(ym.atDay(1));
                        context.setState(AWAITING_MONTHLY_END);
                        bot.deleteMessage(chatId, messageId);

                        bot.sendMessageWithInlineKeyboard(chatId,
                                "Выбрана начальная дата: " + ym + "\nТеперь выберите конечную дату:",
                                CalendarInlineKeyboardUtil.generateMonthKeyboard("monthly_end", ym));
                    } else if (action.equals("year")) {
                        int year = Integer.parseInt(value);
                        bot.deleteMessage(chatId, messageId);
                        bot.sendMessageWithInlineKeyboard(chatId, "Выберите начальный месяц:",
                                CalendarInlineKeyboardUtil.generateMonthKeyboard("monthly_start", YearMonth.of(year, 1)));
                    }
                    break;

                case AWAITING_MONTHLY_END:
                    if (action.equals("select")) {
                        YearMonth ym = YearMonth.parse(value);
                        context.setEndDate(ym.atEndOfMonth());
                        bot.deleteMessage(chatId, messageId);
                        fetchMonthlyStatistics(chatId, bot, context);
                    } else if (action.equals("year")) {
                        int year = Integer.parseInt(value);
                        bot.deleteMessage(chatId, messageId);
                        bot.sendMessageWithInlineKeyboard(chatId, "Выберите конечный месяц:",
                                CalendarInlineKeyboardUtil.generateMonthKeyboard("monthly_end", YearMonth.of(year, 1)));
                    }
                    break;

                case AWAITING_CATEGORY_START:
                    if (action.equals("select")) {
                        YearMonth ym = YearMonth.parse(value);
                        context.setStartDate(ym.atDay(1));
                        context.setState(StatState.AWAITING_CATEGORY_END);
                        bot.deleteMessage(chatId, messageId);
                        bot.sendMessageWithInlineKeyboard(chatId, "Теперь выберите конечный месяц",
                                CalendarInlineKeyboardUtil.generateMonthKeyboard("category_end", ym));
                    } else if (action.equals("year")) {
                        int year = Integer.parseInt(value);
                        bot.deleteMessage(chatId, messageId);
                        bot.sendMessageWithInlineKeyboard(chatId, "Выберите начальный месяц:",
                                CalendarInlineKeyboardUtil.generateMonthKeyboard("category_start", YearMonth.of(year, 1)));
                    }
                    break;

                case AWAITING_CATEGORY_END:
                    if (action.equals("select")) {
                        YearMonth ym = YearMonth.parse(value);
                        context.setEndDate(ym.atEndOfMonth());
                        bot.deleteMessage(chatId, messageId);
                        fetchCategoryStatistics(chatId, bot, context);
                    } else if (action.equals("year")) {
                        int year = Integer.parseInt(value);
                        bot.deleteMessage(chatId, messageId);
                        bot.sendMessageWithInlineKeyboard(chatId, "Выберите конечный месяц:",
                                CalendarInlineKeyboardUtil.generateMonthKeyboard("category_end", YearMonth.of(year, 1)));
                    }
                    break;

                case AWAITING_DAILY_START:
                    if (action.equals("select")) {
                        LocalDate date = LocalDate.parse(value);
                        context.setStartDate(date);
                        context.setState(StatState.AWAITING_DAILY_END);
                        bot.deleteMessage(chatId, messageId);
                        bot.sendMessageWithInlineKeyboard(chatId, "Выберите конечную дату:",
                                CalendarInlineKeyboardUtil.generateDayKeyboard("statistics","daily_end", date));
                    } else if (action.equals("nav")) {
                        LocalDate newDate = LocalDate.parse(value);
                        bot.deleteMessage(chatId, messageId);
                        bot.sendMessageWithInlineKeyboard(chatId, "Выберите начальную дату:",
                                CalendarInlineKeyboardUtil.generateDayKeyboard("statistics","daily_start", newDate));
                    }
                    break;

                case AWAITING_DAILY_END:
                    if (action.equals("select")) {
                        LocalDate date = LocalDate.parse(value);
                        context.setEndDate(date);
                        bot.deleteMessage(chatId, messageId);
                        fetchDailyStatistics(chatId, bot, context);
                    } else if (action.equals("nav")) {
                        LocalDate newDate = LocalDate.parse(value);
                        bot.deleteMessage(chatId, messageId);
                        bot.sendMessageWithInlineKeyboard(chatId, "Выберите конечную дату:",
                                CalendarInlineKeyboardUtil.generateDayKeyboard("statistics","daily_end", newDate));
                    }
                    break;

                case AWAITING_BALANCE_START:
                    if (action.equals("select")) {
                        LocalDate chosen = LocalDate.parse(value);
                        context.setStartDate(chosen);
                        context.setState(StatState.AWAITING_BALANCE_END);
                        bot.deleteMessage(chatId, messageId);
                        bot.sendMessageWithInlineKeyboard(chatId, "Выберите конечную дату:",
                                CalendarInlineKeyboardUtil.generateDayKeyboard("statistics","balance_end", chosen));
                    } else if (action.equals("nav")) {
                        LocalDate newDate = LocalDate.parse(value);
                        bot.deleteMessage(chatId, messageId);
                        bot.sendMessageWithInlineKeyboard(chatId, "Выберите начальную дату:",
                                CalendarInlineKeyboardUtil.generateDayKeyboard("statistics","balance_start", newDate));
                    }
                    break;

                case AWAITING_BALANCE_END:
                    if (action.equals("select")) {
                        LocalDate chosen = LocalDate.parse(value);
                        context.setEndDate(chosen);
                        bot.deleteMessage(chatId, messageId);
                        fetchBalanceSummary(chatId, bot, context);
                    } else if (action.equals("nav")) {
                        LocalDate newDate = LocalDate.parse(value);
                        bot.deleteMessage(chatId, messageId);
                        bot.sendMessageWithInlineKeyboard(chatId, "Выберите конечную дату:",
                                CalendarInlineKeyboardUtil.generateDayKeyboard("statistics","balance_end", newDate));
                    }
                    break;

            }
        } catch (Exception e) {
            log.error("Ошибка в календаре: {}", e.getMessage());
            bot.sendMessage(chatId, "Ошибка обработки даты. Попробуйте снова или /cancel.");
            cancelStatFlow(chatId);
        }
    }

    private void fetchMonthlyStatistics(Long chatId, TelegramBot bot, UserStateContextUtil context) {
        try {
            String token = telegramUserService.getJwtToken(chatId);
            if (token == null) {
                bot.sendMessage(chatId, "Вы не авторизованы. Войдите с помощью /login");
                return;
            }
            YearMonth startMonth = YearMonth.from(context.getStartDate());
            YearMonth endMonth = YearMonth.from(context.getEndDate());

            String url = String.format("%s/monthlyStatistic?startDate=%s&endDate=%s", urlAPI, startMonth, endMonth);
            String response = restClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, Object>> results = objectMapper.readValue(response, List.class);

            if (results.isEmpty()) {
                bot.sendMessage(chatId, "Данных за указанный период нет.");
                return;
            }

            StringBuilder message = new StringBuilder("*Месячная статистика:*\n\n");
            for (Map<String, Object> stat : results) {
                message.append(String.format("%s\nДоходы: %s₽\nРасходы: %s₽\nБаланс: %s₽\n\n",
                        stat.get("month"), stat.get("income"), stat.get("expense"), stat.get("balance")));
            }

            bot.sendMessage(chatId, message.toString());
        } catch (Exception e) {
            log.error("Ошибка получения monthly statistics: {}", e.getMessage());
            bot.sendMessage(chatId, "Ошибка получения статистики: " + e.getMessage());
        } finally {
            cancelStatFlow(chatId);
        }
    }

    public void startCategoryStatFlow(long chatId, TelegramBot bot) {
        UserStateContextUtil context = new UserStateContextUtil();
        context.setState(StatState.AWAITING_CATEGORY_START);
        userContexts.put(chatId, context);

        YearMonth now = YearMonth.now();
        InlineKeyboardMarkup calendar = CalendarInlineKeyboardUtil.generateMonthKeyboard("category_start", now);
        bot.sendMessageWithInlineKeyboard(chatId, "Выберите начальный месяц", calendar);
    }

    public void startBalanceStatFlow(long chatId, TelegramBot bot) {
        UserStateContextUtil context = new UserStateContextUtil();
        context.setState(StatState.AWAITING_BALANCE_START);
        userContexts.put(chatId, context);

        LocalDate now = LocalDate.now();
        InlineKeyboardMarkup calendar = CalendarInlineKeyboardUtil.generateDayKeyboard("statistics","balance_start", now);
        bot.sendMessageWithInlineKeyboard(chatId, "Выберите начальную дату", calendar);
    }

    private void fetchCategoryStatistics(Long chatId, TelegramBot bot, UserStateContextUtil context) {
        try {
            String jwtToken = telegramUserService.getJwtToken(chatId);
            if (jwtToken == null) {
                bot.sendMessage(chatId, "Вы не авторизованы. Пожалуйста, используйте /login");
                return;
            }

            LocalDate start = context.getStartDate();
            LocalDate end = context.getEndDate();

            String url = String.format("%s/categorySummary?startDate=%s&endDate=%s", urlAPI, start, end);
            String response = restClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + jwtToken)
                    .retrieve()
                    .body(String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, Object>> result = objectMapper.readValue(response, List.class);

            if (result.isEmpty()) {
                bot.sendMessage(chatId, "Нет данных за период.");
                return;
            }

            StringBuilder message = new StringBuilder("*Категории:*\n\n");
            for (Map<String, Object> item : result) {
                message.append(String.format("• %s (%s): %s₽\n",
                        item.get("categoryName"),
                        item.get("type"),
                        item.get("totalAmount")));
            }

            bot.sendMessage(chatId, message.toString());
        } catch (Exception e) {
            log.error("Ошибка категории: {}", e.getMessage());
            bot.sendMessage(chatId, "Ошибка получения данных.");
        } finally {
            cancelStatFlow(chatId);
        }
    }
    private void fetchDailyStatistics(Long chatId, TelegramBot bot, UserStateContextUtil context) {
        try {
            String jwtToken = telegramUserService.getJwtToken(chatId);
            if (jwtToken == null) {
                bot.sendMessage(chatId, "Вы не авторизованы. Пожалуйста, используйте команду /login");
                return;
            }

            LocalDate start = context.getStartDate();
            LocalDate end = context.getEndDate();

            if (start == null || end == null) {
                bot.sendMessage(chatId, "Ошибка: не выбраны обе даты. Используйте /cancel для сброса.");
                return;
            }

            if (end.isBefore(start)) {
                bot.sendMessage(chatId, "Ошибка: конечная дата раньше начальной. Пожалуйста, выберите заново.");
                return;
            }

            String url = String.format("%s/dailyDynamics?startDate=%s&endDate=%s", urlAPI, start, end);
            String response = restClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + jwtToken)
                    .retrieve()
                    .body(String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, Object>> stats = objectMapper.readValue(response, List.class);

            if (stats.isEmpty()) {
                bot.sendMessage(chatId, "За выбранный период нет данных");
                return;
            }

            StringBuilder message = new StringBuilder("*Ежедневная статистика:*\n\n");
            for (Map<String, Object> item : stats) {
                String date = (String) item.get("date");
                String type = (String) item.get("type");
                Object totalAmount = item.get("totalAmount");

                message.append(String.format("📅 %s — %s %s₽\n", date, type.equals("INCOME") ? "➕" : "➖", totalAmount));
            }

            bot.sendMessage(chatId, message.toString());
        } catch (Exception e) {
            log.error("Ошибка при получении ежедневной статистики", e);
            bot.sendMessage(chatId, "Произошла ошибка при получении статистики: " + e.getMessage());
        } finally {
            cancelStatFlow(chatId);
        }
    }

    private void fetchBalanceSummary(Long chatId, TelegramBot bot, UserStateContextUtil context) {
        try {
            String jwtToken = telegramUserService.getJwtToken(chatId);
            if (jwtToken == null) {
                bot.sendMessage(chatId, "Вы не авторизованы. Пожалуйста, используйте команду /login");
                return;
            }

            LocalDate startDate = context.getStartDate();
            LocalDate endDate = context.getEndDate();

            if (startDate == null || endDate == null) {
                bot.sendMessage(chatId, "Ошибка: не выбраны обе даты. Используйте /cancel для сброса.");
                return;
            }

            if (endDate.isBefore(startDate)) {
                bot.sendMessage(chatId, "Ошибка: конечная дата раньше начальной. Пожалуйста, выберите заново.");
                return;
            }

            String url = String.format("%s/balanceSummary?startDate=%s&endDate=%s", urlAPI, startDate, endDate);
            String response = restClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + jwtToken)
                    .retrieve()
                    .body(String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> result = objectMapper.readValue(response, Map.class);

            if (result.isEmpty()) {
                bot.sendMessage(chatId, "Нет данных за указанный период.");
                return;
            }

            double income = safeDouble(result.get("totalIncome"));
            double expense = safeDouble(result.get("totalExpense"));
            double balance = safeDouble(result.get("balance"));

            String summary = String.format("""
                *Финансовый итог за выбранный период:*

                💰 Доходы: %.2f ₽
                💸 Расходы: %.2f ₽
                📊 Баланс: %.2f ₽
                """, income, expense, balance);

            bot.sendMessage(chatId, summary);

        } catch (Exception e) {
            log.error("Ошибка получения итоговой суммы", e);
            bot.sendMessage(chatId, "Произошла ошибка при получении данных: " + e.getMessage());
        } finally {
            cancelStatFlow(chatId);
        }
    }
    private double safeDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return 0.0;
        }
    }
}