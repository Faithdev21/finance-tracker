package com.example.financetracker.telegram.util;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class CalendarInlineKeyboardUtil {

    public static InlineKeyboardMarkup generateMonthKeyboard(String type, YearMonth currentYearMonth) {
        int year = currentYearMonth.getYear();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> yearRow = Arrays.asList(
                button("◀", "statistics:" + type + ":year:" + (year - 1)),
                button(String.valueOf(year), "noop"),
                button("▶", "statistics:" + type + ":year:" + (year + 1))
        );
        rows.add(yearRow);

        for (int i = 1; i <= 12; i += 3) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            for (int j = i; j < i + 3; j++) {
                YearMonth ym = YearMonth.of(year, j);
                String text = ym.getMonth().getDisplayName(TextStyle.SHORT, new Locale("ru"));
                String callbackData = String.format("statistics:%s:select:%s", type, ym);
                row.add(button(text, callbackData));
            }
            rows.add(row);
        }

        return new InlineKeyboardMarkup(rows);
    }

    private static InlineKeyboardButton button(String text, String data) {
        InlineKeyboardButton btn = new InlineKeyboardButton();
        btn.setText(text);
        btn.setCallbackData(data);
        return btn;
    }

    public static InlineKeyboardMarkup generateDayKeyboard(String prefix, String type, LocalDate refDate) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> navRow = Arrays.asList(
                button("◀", prefix + ":" + type + ":nav:" + refDate.minusMonths(1)),
                button(refDate.format(DateTimeFormatter.ofPattern("LLLL yyyy", new Locale("ru"))), "noop"),
                button("▶", prefix + ":" + type + ":nav:" + refDate.plusMonths(1))
        );
        rows.add(navRow);

        String[] weekdays = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
        List<InlineKeyboardButton> weekRow = Arrays.stream(weekdays)
                .map(day -> button(day, "noop"))
                .collect(Collectors.toList());
        rows.add(weekRow);

        LocalDate firstDay = refDate.withDayOfMonth(1);
        int daysInMonth = refDate.lengthOfMonth();
        int firstWeekDay = firstDay.getDayOfWeek().getValue();

        List<InlineKeyboardButton> row = new ArrayList<>();

        for (int i = 1; i < firstWeekDay; i++) {
            row.add(button(" ", "noop"));
        }

        for (int day = 1; day <= daysInMonth; day++) {
            if (row.size() == 7) {
                rows.add(row);
                row = new ArrayList<>();
            }

            LocalDate date = refDate.withDayOfMonth(day);
            row.add(button(String.valueOf(day), prefix + ":" + type + ":select:" + date));
        }

        if (!row.isEmpty()) {
            while (row.size() < 7) {
                row.add(button(" ", "noop"));
            }
            rows.add(row);
        }

        return new InlineKeyboardMarkup(rows);
    }
}