package com.example.financetracker.telegram;

import com.example.financetracker.entity.TelegramUserEntity;
import com.example.financetracker.repository.TelegramUserRepository;
import com.example.financetracker.util.JwtTokenUtils;
import com.example.financetracker.service.TelegramUserService;
import com.example.financetracker.telegram.handlers.*;
import com.example.financetracker.telegram.util.KeyboardUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.springframework.web.client.RestClient;

import jakarta.annotation.PostConstruct;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Autowired
    private TelegramUserService telegramUserService;

    @Autowired
    private RestClient restClient;

    @Autowired
    private JwtTokenUtils jwtTokenUtils;

    @Autowired
    private CommandHandler commandHandler;

    @Autowired
    private StatisticsHandler statisticsHandler;

    @Autowired
    private BudgetHandler budgetHandler;

    @Autowired
    private CategoryHandler categoryHandler;

    @Autowired
    private TransactionHandler transactionHandler;

    @Autowired
    private TelegramUserRepository telegramUserRepository;

    @Autowired
    private QuestHandler questHandler;

    private final Map<Long, UserState> userStates = new HashMap<>();

    static final String urlAPI = "http://localhost:8189//financeTracker";

    private final long botStartTime = Instant.now().getEpochSecond();


    @PostConstruct
    public void init() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(this);
            log.info("Бот запущен в {}", Instant.ofEpochSecond(botStartTime));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();
            int messageId = update.getMessage().getMessageId();

            long messageTimestamp = update.getMessage().getDate();
            if (messageTimestamp < botStartTime) {
                log.info("Игнорируем старое сообщение с timestamp {} (бот запущен в {})", messageTimestamp, botStartTime);
                return;
            }

            if (messageText.equals("/cancel")) {
                handleCancel(chatId);
                return;
            }

            if (questHandler.isUserInQuest(chatId)) {
                questHandler.handleQuestInput(chatId, messageText);
                return;
            }

            if (transactionHandler.isUserInTransactionState(chatId)) {
                transactionHandler.handlePerformTransaction(chatId, this, messageText);
                return;
            }

            if (categoryHandler.isUserInCategoryAddingState(chatId)) {
                categoryHandler.handleAddCategory(chatId, this, messageText);
                return;
            }

            UserState state = userStates.get(chatId);
            if (state != null) {
                state.addMessageId(messageId);
                handleUserState(chatId, messageText, state);
            } else if (messageText.equals("/start")) {
                commandHandler.handleStartCommand(chatId, this);
            } else if (messageText.equals("/login")) {
                startLoginProcess(chatId);
            } else if (messageText.equals("/register")) {
                startRegistrationProcess(chatId);
            } else if (messageText.equals("/logout")) {
                handleLogout(chatId);
            } else {
                handleAuthenticatedAction(chatId, messageText);
            }
        }
    }

    private void startLoginProcess(long chatId) {
        UserState state = new UserState(State.AWAITING_LOGIN);
        userStates.put(chatId, state);
        sendMessage(chatId, "Пожалуйста, введите ваш логин (или используйте /cancel для отмены):", state);
    }

    private void startRegistrationProcess(long chatId) {
        UserState state = new UserState(State.AWAITING_REGISTRATION_USERNAME);
        userStates.put(chatId, state);
        sendMessage(chatId, "Пожалуйста, введите имя пользователя для регистрации (или используйте /cancel для отмены):");
    }

    private void handleUserState(long chatId, String messageText, UserState state) {
        Map<String, String> data = state.getAllData();
        switch (state.getState()) {
            case AWAITING_LOGIN:
                data.put("username", messageText);
                userStates.put(chatId, new UserState(State.AWAITING_PASSWORD, data, state.getMessageIds()));
                sendMessage(chatId, "Пожалуйста, введите ваш пароль (или используйте /cancel для отмены):", userStates.get(chatId));
                break;
            case AWAITING_PASSWORD:
                data.put("password", messageText);
                handleLogin(chatId, data.get("username"), messageText, state);
                break;
            case AWAITING_REGISTRATION_USERNAME:
                data.put("username", messageText);
                userStates.put(chatId, new UserState(State.AWAITING_REGISTRATION_PASSWORD, data, state.getMessageIds()));
                sendMessage(chatId, "Пожалуйста, введите пароль для регистрации (или используйте /cancel для отмены):", userStates.get(chatId));
                break;
            case AWAITING_REGISTRATION_PASSWORD:
                data.put("password", messageText);
                userStates.put(chatId, new UserState(State.AWAITING_REGISTRATION_CONFIRM_PASSWORD, data, state.getMessageIds()));
                sendMessage(chatId, "Пожалуйста, подтвердите пароль (или используйте /cancel для отмены):", userStates.get(chatId));
                break;
            case AWAITING_REGISTRATION_CONFIRM_PASSWORD:
                data.put("confirmPassword", messageText);
                userStates.put(chatId, new UserState(State.AWAITING_REGISTRATION_EMAIL, data, state.getMessageIds()));
                sendMessage(chatId, "Пожалуйста, введите ваш email (или используйте /cancel для отмены):", userStates.get(chatId));
                break;
            case AWAITING_REGISTRATION_EMAIL:
                data.put("email", messageText);
                handleRegistration(chatId, data.get("username"), data.get("password"), data.get("confirmPassword"), data.get("email"), state);
                break;
            default:
                sendMessage(chatId, "Неизвестное состояние. Используйте /start для начала работы.");
        }
    }

    private void handleLogin(long chatId, String login, String password, UserState state) {
        try {
            String url = urlAPI+"/auth";
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("username", login);
            requestBody.put("password", password);

            Map<String, String> response = restClient.post()
                    .uri(url)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("token")) {
                String jwtToken = response.get("token");
                log.info("JWT TOKEN - {}", jwtToken);
                String refreshToken = response.get("refreshToken");
                telegramUserService.saveJwtToken(chatId, jwtToken, refreshToken, login);
                deleteMessages(chatId, state.getMessageIds());
                userStates.remove(chatId);
                sendMessage(chatId, "Аутентификация успешна!");
                sendMainMenu(chatId);
            } else {
                userStates.remove(chatId);
                sendMessage(chatId, "Ошибка авторизации. Попробуйте снова.");
                startLoginProcess(chatId);
            }
        } catch (Exception e) {
            userStates.remove(chatId);
            sendMessage(chatId, "Ошибка авторизации. Попробуйте снова.");
            startLoginProcess(chatId);
        }
    }

    private void handleRegistration(long chatId, String username, String password, String confirmPassword, String email, UserState state) {
        try {
            String url = urlAPI+"/registration";
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("username", username);
            requestBody.put("password", password);
            requestBody.put("confirmPassword", confirmPassword);
            requestBody.put("email", email);

            Map<String, Object> response = restClient.post()
                    .uri(url)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (response != null) {
                deleteMessages(chatId, state.getMessageIds());
                userStates.remove(chatId);
                sendMessage(chatId, "Регистрация успешна! Теперь выполните вход с помощью команды /login.");
            } else {
                deleteMessages(chatId, state.getMessageIds());
                userStates.remove(chatId);
                sendMessage(chatId, "Ошибка регистрации. Попробуйте снова.");
                startRegistrationProcess(chatId);
            }
        } catch (Exception e) {
            deleteMessages(chatId, state.getMessageIds());
            userStates.remove(chatId);
            sendMessage(chatId, "Ошибка регистрации: " + e.getMessage() + ". Попробуйте снова.");
            startRegistrationProcess(chatId);
        }
    }

    private void handleLogout(long chatId) {
        telegramUserService.logout(chatId);
        sendMessage(chatId, "Вы успешно вышли из профиля. Для входа в другой профиль используйте /login.");
    }

    private void handleCancel(long chatId) {
        userStates.remove(chatId);
        categoryHandler.resetUserState(chatId);
        transactionHandler.resetUserState(chatId);
        questHandler.resetQuestState(chatId);
        removeKeyboard(chatId, "Действие отменено.");
        sendMainMenu(chatId);
    }

    private void handleAuthenticatedAction(long chatId, String messageText) {
        try {
            String jwtToken = telegramUserService.getJwtToken(chatId);
            if (!jwtTokenUtils.validateToken(jwtToken)) {
                refreshToken(chatId);
                jwtToken = telegramUserService.getJwtToken(chatId);
            }
            switch (messageText) {
                case "📊 Статистика":
                    sendStatisticsMenu(chatId);
                    break;
                case "💰 Бюджет":
                    sendBudgetMenu(chatId);
                    break;
                case "📋 Категории":
                    sendCategoriesMenu(chatId);
                    break;
                case "💸 Транзакции":
                    sendTransactionsMenu(chatId);
                    break;
                case "📅 Месячная статистика":
                    statisticsHandler.handleMonthlyStatistics(chatId, this);
                    break;
                case "📈 Ежедневная статистика":
                    statisticsHandler.handleDailyStatistics(chatId, this);
                    break;
                case "📊 Статистика по категориям":
                    statisticsHandler.handleCategoryStatistics(chatId, this);
                    break;
                case "💵 Итоговая сумма":
                    statisticsHandler.handleBalanceSummary(chatId, this);
                    break;
                case "📝 Установить бюджет":
                    budgetHandler.handleSetBudget(chatId, this);
                    break;
                case "✏️ Изменить бюджет":
                    budgetHandler.handleUpdateBudget(chatId, this);
                    break;
                case "🗑️ Удалить бюджет":
                    budgetHandler.handleDeleteBudget(chatId, this);
                    break;
                case "📋 Список бюджетов":
                    budgetHandler.handleListBudgets(chatId, this);
                    break;
                case "➕ Добавить статью доходов/расходов":
                    categoryHandler.handleAddCategory(chatId, this, messageText);
                    break;
                case "📋 Мои статьи доходов/расходов":
                    categoryHandler.handleListCategories(chatId, this);
                    break;
                case "💸 Выполнить транзакцию":
                    transactionHandler.handlePerformTransaction(chatId, this, messageText);
                    break;
                case "📜 Мои транзакции":
                    transactionHandler.handleListTransactions(chatId, this);
                    break;
                case "🔙 Назад":
                    sendMainMenu(chatId);
                    break;
                case "🔒 Секретный код":
                    questHandler.handleStartQuest(chatId);
                    break;
                default:
                    sendMessage(chatId, "Команда не найдена :(" + "\n" + "Пожалуйста, выберите действие из меню.");
            }
        } catch (RuntimeException e) {
            sendMessage(chatId, "Вы не авторизованы. Пожалуйста, выполните вход с помощью команды /login.");
        }
    }

    public void refreshToken(long chatId) {
        try {
            String refreshToken = telegramUserService.getRefreshToken(chatId);
            if (refreshToken == null) {
                throw new RuntimeException("Refresh-токен отсутствует. Пожалуйста, выполните вход заново.");
            }

            String url = urlAPI+"/auth/refresh";
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("refreshToken", refreshToken);

            Map<String, String> response = restClient.post()
                    .uri(url)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("token")) {
                TelegramUserEntity user = telegramUserRepository.findByTelegramId(chatId);
                String newJwtToken = response.get("token");
                telegramUserService.saveJwtToken(chatId, newJwtToken, refreshToken, user.getUsername());
                sendMessage(chatId, "Токен успешно обновлён.");
            } else {
                throw new RuntimeException("Ошибка обновления токена.");
            }
        } catch (Exception e) {
            telegramUserService.logout(chatId);
            sendMessage(chatId, "Сессия истекла. Пожалуйста, выполните вход заново с помощью команды /login.");
        }
    }

    public void sendMainMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите команду из меню:");
        message.setReplyMarkup(KeyboardUtil.createMainMenuKeyboard());
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendStatisticsMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите тип статистики:");
        message.setReplyMarkup(KeyboardUtil.createStatisticsMenuKeyboard());
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendBudgetMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите действие с бюджетом:");
        message.setReplyMarkup(KeyboardUtil.createBudgetMenuKeyboard());
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendCategoriesMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите действие с категориями:");
        message.setReplyMarkup(KeyboardUtil.createCategoriesMenuKeyboard());
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendTransactionsMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите действие с транзакциями:");
        message.setReplyMarkup(KeyboardUtil.createTransactionsMenuKeyboard());
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setParseMode("MarkdownV2");
        message.setChatId(String.valueOf(chatId));
        message.setText(escapeMarkdownV2(text));
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private void sendMessage(long chatId, String text, UserState state) {
        SendMessage message = new SendMessage();
        message.setParseMode("MarkdownV2");
        message.setChatId(String.valueOf(chatId));
        message.setText(escapeMarkdownV2(text));
        try {
            Message sentMessage = execute(message);
            if (state != null) {
                state.addMessageId(sentMessage.getMessageId());
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageWithKeyboard(long chatId, String text, ReplyKeyboardMarkup keyboard) {
        SendMessage message = new SendMessage();
        message.setParseMode("MarkdownV2");
        message.setChatId(String.valueOf(chatId));
        message.setText(escapeMarkdownV2(text));
        message.setReplyMarkup(keyboard);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void removeKeyboard(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setParseMode("MarkdownV2");
        message.setChatId(String.valueOf(chatId));
        message.setText(escapeMarkdownV2(text));
        message.setReplyMarkup(new ReplyKeyboardRemove(true));
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private void deleteMessages(long chatId, List<Integer> messageIds) {
        for (Integer messageId : messageIds) {
            try {
                DeleteMessage deleteMessage = new DeleteMessage();
                deleteMessage.setChatId(String.valueOf(chatId));
                deleteMessage.setMessageId(messageId);
                execute(deleteMessage);
            } catch (TelegramApiException e) {
                log.warn("Не удалось удалить сообщение с ID {}: {}", messageId, e.getMessage());
            }
        }
    }

    public void sendPhoto(long chatId, String photoPath, String caption) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(String.valueOf(chatId));
        sendPhoto.setPhoto(new InputFile(photoPath));
        if (caption != null) {
            sendPhoto.setCaption(caption);
        }
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            sendMessage(chatId, "Ошибка при отправке изображения" + e.getMessage());
        }
    }

    public static String escapeMarkdownV2(String text) {
        return text
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace(">", "\\>")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace("=", "\\=")
                .replace("|", "\\|")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace(".", "\\.")
                .replace("!", "\\!");
    }

}

class UserState {
    @Getter
    private final State state;
    private final Map<String, String> data;
    private final List<Integer> messageIds;

    public UserState(State state) {
        this(state, new HashMap<>(), new ArrayList<>());
    }

    public UserState(State state, Map<String, String> data) {
        this(state, data, new ArrayList<>());
    }

    public UserState(State state, Map<String, String> data, List<Integer> messageIds) {
        this.state = state;
        this.data = data;
        this.messageIds = messageIds;
    }

    public Map<String, String> getAllData() {
        return new HashMap<>(data);
    }

    public void addMessageId(int messageId) {
        this.messageIds.add(messageId);
    }

    public List<Integer> getMessageIds() {
        return new ArrayList<>(messageIds);
    }
}

enum State {
    AWAITING_LOGIN,
    AWAITING_PASSWORD,
    AWAITING_REGISTRATION_USERNAME,
    AWAITING_REGISTRATION_PASSWORD,
    AWAITING_REGISTRATION_CONFIRM_PASSWORD,
    AWAITING_REGISTRATION_EMAIL

}