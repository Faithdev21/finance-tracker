package com.example.financetracker.telegram.util;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class KeyboardUtil {

    public static ReplyKeyboardMarkup createMainMenuKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("📊 Статистика");
        row1.add("💰 Бюджет");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("📋 Категории");
        row2.add("💸 Транзакции");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("🔒 Секретный код");

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        return keyboardMarkup;
    }

    public static ReplyKeyboardMarkup createStatisticsMenuKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("📅 Месячная статистика");
        row1.add("📈 Ежедневная статистика");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("📊 Статистика по категориям");
        row2.add("💵 Итоговая сумма");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("🔙 Назад");

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        return keyboardMarkup;
    }

    public static ReplyKeyboardMarkup createBudgetMenuKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("📝 Установить бюджет");
        row1.add("✏️ Изменить бюджет");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("🗑️ Удалить бюджет");
        row2.add("📋 Список бюджетов");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("🔙 Назад");

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        return keyboardMarkup;
    }

    public static ReplyKeyboardMarkup createCategoriesMenuKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("➕ Добавить статью доходов/расходов");
        row1.add("📋 Мои статьи доходов/расходов");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("🔙 Назад");

        keyboard.add(row1);
        keyboard.add(row2);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        return keyboardMarkup;
    }

    public static ReplyKeyboardMarkup createTransactionsMenuKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("💸 Выполнить транзакцию");
        row1.add("📜 Мои транзакции");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("🔙 Назад");

        keyboard.add(row1);
        keyboard.add(row2);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        return keyboardMarkup;
    }
}