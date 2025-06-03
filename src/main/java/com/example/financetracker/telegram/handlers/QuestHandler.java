package com.example.financetracker.telegram.handlers;

import com.example.financetracker.telegram.TelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.*;

@Component
public class QuestHandler {

    private static final String[] MEMORY_BUTTON_LABELS = {
            "Дурачились 😜" ,
            "Умничали 🧠",
            "Путешествовали ✈️",
            "Обнимались 🤗",
            "Радовались 🥰",
            "Мечтали 🌠",
            "Любили ❤️"
    };

    private static final String[] MEMORY_TEXTS = {
            """
            Моменты, когда мы позволяли себе быть просто беззаботными! 😜 Эти дурачества всегда заряжали энергией! ✨""",
            """
            Наши глубокие разговоры и умные мысли! 🧠 В такие моменты я восхищаюсь твоим мышлением! 💡""",
            """
            Каждый наш выход куда угодно — это целый мир, который мы открывали вместе! 🗺️""",
            """
            Самые теплые, уютные и искренние объятия! 🤗 В них я чувствую себя по-настоящему дома. 🏡💖""",
            """
            Та чистая, искренняя радость, которую ты даришь мне одним своим присутствием и улыбкой! 🥰 Эти моменты — самое ценное в моей жизни! ✨""",
            """
            Наши бескрайние мечты! 🌌 Каждый раз, когда мы мечтали о будущем, мир становился чуточку ближе к нам! ✨""",
            """
            Это чувство, которое пронизывает каждый миг нашей жизни. ❤️ Наша любовь — самый яркий свет и смысл всего, что мы делаем! 🌟"""
    };

    private static final String[] MEMORY_PHOTO_PATHS = {
            "https://disk.yandex.ru/i/j-N7v3iVfrb6tg",
            "https://disk.yandex.ru/i/gO-sy91XX3IrRA",
            "https://disk.yandex.ru/i/CGkmtSqAEWL-sQ",
            "https://disk.yandex.ru/i/q-6NyH3di7xG3g",
            "https://disk.yandex.ru/i/wnkIjqEpu9s0UQ",
            "https://disk.yandex.ru/i/4a5YhmNz_rs7Ow",
            "https://disk.yandex.ru/i/K_K22SVUKVRgRQ"
    };
    private static final String[] SEQUENCE_BUTTON_LABELS = {
            """
            Дойти до военкомата 👫""",
            """
            Ещё 💡""",
            """
            Дальше ▶️""",
            """
            Ещё ещё 📍""",
            """
            Продолжаем 🛤️""",
            """
            Осталось немного 🕰️""",
            """
            Вперед к дембелю! 🚀""",
            """
            Возвращаемся в реальность 🏁"""
    };
    private static final String[] SEQUENCE_PHOTO_PATHS = {
            "https://disk.yandex.ru/i/UyyWmNY3eV7ZlQ",
            "https://disk.yandex.ru/i/n2-0WClhwshTqg",
            "https://disk.yandex.ru/i/mPtSa7Z8HuOjKA",
            "https://disk.yandex.ru/i/ufXpdfP2F0ticw",
            "https://disk.yandex.ru/i/daGGrio5Hzn-XA",
            "https://disk.yandex.ru/i/ur4Ttj5vQ192hQ",
            "https://disk.yandex.ru/i/8enHs_EA66Yjkg",
            "https://disk.yandex.ru/i/gI5YDQT9UjCxfA"
    };
    private static final String[] SEQUENCE_TEXTS = {
            """
            Ого! 🤯 Что это еще такое??? Кажется, мы застряли во времени, и чтобы вернуться в наше настоящее, мне нужна именно твоя помощь! ⏳✨ *Всё в твоих руках, моя дорогая!*
            """,
            """
            Так, дошли... 🚶‍♀️
            """,
            """
            О, да это же присяга 🎖️
            """,
            """
            А это наше *первое увольнение в Москве*! 🏙️ Помнишь, как было здорово? 😊
            """,
            """
            И вот... ещё одна твоя *героическая вылазка* в Калининец! 😄 Каждый раз как приключение!
            """,
            """
            Ура, уже наступил 2025! Всё ближе к цели! 🚀
            """,
            """
            Стали еще на пару месяцев ближе к финалу! 🗓️
            """,
            """
            Осталось совсем чуть-чуть до возвращения в реальность, *моя хорошая!*💖
            """
    };

    private final Map<Long, QuestState> userQuestStates = new HashMap<>();
    private final Map<Long, Integer> userMemoryButtonIndex = new HashMap<>();
    private final Map<Long, Integer> userSequenceStep = new HashMap<>();

    @Autowired
    private TelegramBot bot;

    public enum QuestState {
        NONE,
        AWAITING_BIRTH_DATE,
        AWAITING_RIDDLE_1,
        AWAITING_RIDDLE_2,
        AWAITING_MEMORY_BUTTONS,
        AWAITING_CONFIRMATION,
        AWAITING_SEQUENCE,
        COMPLETED
    }

    public boolean isUserInQuest(long chatId) {
        return userQuestStates.containsKey(chatId) && userQuestStates.get(chatId) != QuestState.NONE;
    }

    public void resetQuestState(long chatId) {
        userQuestStates.remove(chatId);
        userMemoryButtonIndex.remove(chatId);
        userSequenceStep.remove(chatId);
    }

    public void handleStartQuest(long chatId) {
        userQuestStates.put(chatId, QuestState.AWAITING_BIRTH_DATE);
        bot.sendMessage(chatId, "\uD83C\uDF1F Введите дату вашего рождения! (_формат - дд.мм.гггг_)");
    }

    public void handleQuestInput(long chatId, String messageText) {
        if (messageText.equalsIgnoreCase("Выйти")) {
            resetQuestState(chatId);
            bot.removeKeyboard(chatId, "Вы вышли из квеста");
            bot.sendMainMenu(chatId);
            return;
        }

        QuestState state = userQuestStates.getOrDefault(chatId, QuestState.NONE);
        switch (state) {
            case AWAITING_BIRTH_DATE:
                handleBirthDateInput(chatId, messageText);
                break;
            case AWAITING_RIDDLE_1:
                handleRiddle1Input(chatId, messageText);
                break;
            case AWAITING_RIDDLE_2:
                handleRiddle2Input(chatId, messageText);
                break;
            case AWAITING_MEMORY_BUTTONS:
                handleMemoryButtonInput(chatId, messageText);
                break;
            case AWAITING_CONFIRMATION:
                handleConfirmationInput(chatId, messageText);
                break;
            case AWAITING_SEQUENCE:
                handleSequenceInput(chatId, messageText);
                break;
            default:
                bot.sendMessage(chatId, "Ты была близка, но ответ другой ♥️");
        }
    }

    private void handleBirthDateInput(long chatId, String messageText) {
        if (messageText.equals("15.06.2003") || messageText.equals("15.06.03")) {
            userQuestStates.put(chatId, QuestState.AWAITING_RIDDLE_1);
            bot.sendMessage(chatId, "Какая чудесная дата!");
            bot.sendMessage(chatId, """
                    🎁 Тебе предстоит отгадать загадку, чтобы войти в квест :)
                    
                    Сначала это было один раз и аккуратно, затем переросло в повседневное и нежное.
                    
                    Мне кажется, что ты поймешь подсказку. И когда отгадаешь - не забудь написать слово сюда. Люблю тебя очень сильно. А еще ты самая лучшая на свете! Я жду💌
                    """);
        } else {
            bot.sendMessage(chatId, "Неверная дата, повторите еще раз!");
        }
    }

    private void handleRiddle1Input(long chatId, String messageText) {
        if (messageText.equals("Милая") || messageText.equals("милая")) {
            bot.sendMessage(chatId, """
                    💖 Всё так, *милая моя*!
                    
                    🧠 Я знал, что ты легко справишься, но это только начало...
                    
                    🎈 *Я подготовил для тебя квест к самому замечательному дню — твоему Дню Рождения!* 🎁
                    
                    P.S. Надеюсь, что всё будет работать как надо и пройти квест не составит большого труда ☺️
                    """);
            bot.sendMessage(chatId, """
                    Давай вспомним как это было чуть больше года назад? 💫
                    
                    Был очень теплый конец весны, мы с тобой были самыми счастливыми людьми на планете!
                    Для меня было иаким счастьем проводить время с тобой! 💕
                    
                    Тебе нужно найти одну из составляющих математической матрицы, а именно президента, 15 21 18 и я жду твоего сообщения.
                    Дальше посчитай количество красных и отправь ответ сюда 💌
                    """);
            userQuestStates.put(chatId, QuestState.AWAITING_RIDDLE_2);
        } else {
            bot.sendMessage(chatId, "Ты была близка, милая, но ответ другой ♥️");
        }
    }

    private void handleRiddle2Input(long chatId, String messageText) {
        if (messageText.equals("24")) {
            bot.sendMessage(chatId, """
                    🎉 Всё верно, именно столько тебе сегодня исполняется 🥳
                    
                    Или... нет? Это же столько лет мне! 😅
                    
                    *А тебе вот столько!*
                    """);
            bot.sendPhoto(chatId, "https://disk.yandex.ru/i/l8XtMdwthBBAjQ", null);
            bot.sendMessage(chatId, """
                    📸 Ты успешно справилась со вторым испытанием и заодно смогла пересмотреть прекрасные моменты из прошлого:)
                    
                    🌈 Как насчет немного отвлечься от загадок и пробежаться пробежаться по спектру наших эмоций?
                    """);
            userQuestStates.put(chatId, QuestState.AWAITING_MEMORY_BUTTONS);
            handleMemoryButtons(chatId, 0);
        } else {
            bot.sendMessage(chatId, "Ты была близка, но ответ другой ♥️");
        }
    }

    private void handleMemoryButtons(long chatId, Integer buttonIndex) {

        userMemoryButtonIndex.put(chatId, buttonIndex);

        if (buttonIndex < MEMORY_BUTTON_LABELS.length) {
            ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
            keyboard.setResizeKeyboard(true);
            List<KeyboardRow> rows = new ArrayList<>();
            KeyboardRow row = new KeyboardRow();
            row.add(MEMORY_BUTTON_LABELS[buttonIndex]);
            rows.add(row);
            keyboard.setKeyboard(rows);

            bot.sendMessageWithKeyboard(chatId, "Посмотрим что там дальше?", keyboard);
        }
    }

    private void handleMemoryButtonInput(long chatId, String messageText) {
        int currentIndex = userMemoryButtonIndex.getOrDefault(chatId, 0);

        if (messageText.equals(MEMORY_BUTTON_LABELS[currentIndex])) {
            bot.sendPhoto(chatId, MEMORY_PHOTO_PATHS[currentIndex], MEMORY_TEXTS[currentIndex]);

            if (currentIndex < MEMORY_BUTTON_LABELS.length - 1) {
                handleMemoryButtons(chatId, currentIndex + 1);
            } else {
                userQuestStates.put(chatId, QuestState.AWAITING_CONFIRMATION);
                ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
                keyboard.setResizeKeyboard(true);
                List<KeyboardRow> rows = new ArrayList<>();
                KeyboardRow row = new KeyboardRow();
                row.add("Я справилась!");
                rows.add(row);
                keyboard.setKeyboard(rows);

                bot.sendMessageWithKeyboard(chatId, """
                        🧩 *А вот тебе следующая головоломка!*
                        
                        Тебе предстоит переключиться и понять, что означает сокровищница и понять как с этим связано кваканье 🐸
                        Когда выполнишь задание — улыбнись и тыкни на кнопочку ниже ☀️
                        """, keyboard);
            }
        } else {
            bot.sendMessage(chatId, "Ты была близка, но ответ другой ♥️");
        }
    }

    private void handleConfirmationInput(long chatId, String messageText) {
        if (messageText.equals("Я справилась!")) {
            userQuestStates.put(chatId, QuestState.AWAITING_SEQUENCE);
            userSequenceStep.put(chatId, 0);

            bot.sendPhoto(chatId, SEQUENCE_PHOTO_PATHS[0], null);

            ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
            keyboard.setResizeKeyboard(true);
            List<KeyboardRow> rows = new ArrayList<>();
            KeyboardRow row = new KeyboardRow();
            row.add(SEQUENCE_BUTTON_LABELS[0]);
            rows.add(row);
            keyboard.setKeyboard(rows);

            bot.sendMessageWithKeyboard(chatId, """
            Ого! 🤯 Что это еще такое??? Кажется, мы застряли во времени, и чтобы вернуться в наше настоящее, мне нужна именно твоя помощь! ⏳✨ *Всё в твоих руках, моя дорогая!*""", keyboard);
        } else {
            bot.sendMessage(chatId, "Ты была близка, но ответ другой ♥️");
        }
    }

    private void handleSequenceInput(long chatId, String messageText) {
        int currentStep = userSequenceStep.getOrDefault(chatId, 0);
        String expectedButton = SEQUENCE_BUTTON_LABELS[currentStep];

        if (messageText.equals(expectedButton)) {
            if (currentStep < SEQUENCE_BUTTON_LABELS.length - 1) {
                userSequenceStep.put(chatId, currentStep + 1);

                bot.sendPhoto(chatId, SEQUENCE_PHOTO_PATHS[currentStep + 1], null);

                ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
                keyboard.setResizeKeyboard(true);
                List<KeyboardRow> rows = new ArrayList<>();
                KeyboardRow row = new KeyboardRow();
                row.add(SEQUENCE_BUTTON_LABELS[currentStep + 1]);
                rows.add(row);
                keyboard.setKeyboard(rows);

                bot.sendMessageWithKeyboard(chatId, SEQUENCE_TEXTS[currentStep + 1], keyboard);
            } else {
                ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
                keyboard.setResizeKeyboard(true);
                List<KeyboardRow> rows = new ArrayList<>();
                KeyboardRow row = new KeyboardRow();
                row.add("Завершить путешествие");
                rows.add(row);
                keyboard.setKeyboard(rows);

                bot.sendMessageWithKeyboard(chatId, """
                        Ой, а тут должна быть ты с цветочками!💐 Но где же они?
                        А они в пути!
                        
                        Жду фотографию в личку! 📨
                        """, keyboard);
            }
        } else if (messageText.equals("Завершить путешествие") && currentStep == SEQUENCE_BUTTON_LABELS.length - 1) {
            userQuestStates.put(chatId, QuestState.COMPLETED);
            bot.sendPhoto(chatId, "https://disk.yandex.ru/i/Bb91nz0jiNqCqQ", null);
            bot.removeKeyboard(chatId, """
                    Квест завершен, ты справилась ♥️
                    
                    🌷 Я БЕЗУМНО сильно люблю тебя, *родная*!
                    Надеюсь мой небольшой подарок тебе понравился, креативил как мог и с чем мог 💘
                    
                    Ты вдохновляешь, поддерживаешь, наполняешь мою жизнь смыслом 🤍
                    
                    Никогда не забывай, как *я сильно тебя люблю* 🌌
                    
                    Ты самый важный человек в моей жизни, оставайся же такой всегда! 💞
                    
                    Твой Егор 💋
                    """);
            bot.sendMainMenu(chatId);
            resetQuestState(chatId);
        } else {
            bot.sendMessage(chatId, "Ты была близка, но ответ другой ♥️");
        }
    }
}
