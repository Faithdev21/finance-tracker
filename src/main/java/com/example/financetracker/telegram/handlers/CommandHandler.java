package com.example.financetracker.telegram.handlers;

import com.example.financetracker.telegram.TelegramBot;
import org.springframework.stereotype.Component;

@Component
public class CommandHandler {

    public void handleStartCommand(long chatId, TelegramBot bot) {
        bot.sendMessage(chatId, """
                Добро пожаловать в Finance Tracker Bot!
                Для начала работы зарегистрируйтесь с помощью команды /register и выполните вход с помощью команды /login.
                Для выхода из профиля введите /logout"""
                );
    }
}