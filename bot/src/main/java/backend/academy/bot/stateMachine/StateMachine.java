package backend.academy.bot.stateMachine;

import backend.academy.bot.command.CommandHandler;
import backend.academy.bot.dto.scrapperAPI.request.LinkUpdate;
import backend.academy.bot.dto.scrapperAPI.response.LinkResponse;
import backend.academy.bot.exception.ApiError;
import backend.academy.bot.service.ScrapperClient;
import backend.academy.bot.service.TelegramClient;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class StateMachine {
    public static final String CHAT_ID = "chatId";
    public static final String START = "/start";
    public static final String SET_LINK_ERROR = "Ошибка при добавлении ссылки: ";
    // Хранение разговоров для каждого пользователя (chatId)
    private final Map<Long, Conversation> conversations = new ConcurrentHashMap<>();
    private final TelegramClient telegramClient;
    private final ScrapperClient scrapperClient;
    private final Logger logger = LoggerFactory.getLogger(StateMachine.class);
    private final Map<String, CommandHandler> commandHandlers = new HashMap<>();

    public StateMachine(TelegramClient telegramClient, ScrapperClient scrapperClient, List<CommandHandler> handlers) {
        this.telegramClient = telegramClient;
        this.scrapperClient = scrapperClient;
        for (CommandHandler handler : handlers) {
            commandHandlers.put(handler.getCommand().toLowerCase(), handler);
        }
    }

    @SuppressWarnings("checkstyle:ReturnCount")
    public void start(Long chatId, String messageText) {
        Conversation conv = conversations.computeIfAbsent(chatId, id -> new Conversation());
        State state = conv.getState();
        if (state == State.START) {
            startScript(chatId, messageText, conv);
            return;
        }
        if (state == State.COMMAND_WAITING) {
            processCommand(chatId, messageText, conv);
            return;
        }
        switch (state) {
            case ADD_LINK:
                addLinkScript(chatId, messageText, conv);
                break;
            case ASK_ABT_TAG:
                askAbtTagScript(chatId, messageText, conv);
                break;
            case WAITING_FOR_TAGS:
                waitingForTagsScript(chatId, messageText, conv);
                break;
            case WAITING_FOR_FILTERS:
                waitingForFiltersScript(chatId, messageText, conv);
                break;
            default:
                telegramClient.sendMessage(chatId, "Неверное состояние. Введите /help для получения списка команд.");
                conv.reset();
                logger.error("Недопустимое состояние", StructuredArguments.keyValue(CHAT_ID, chatId));
        }
    }

    private void waitingForFiltersScript(Long chatId, String messageText, Conversation conv) {
        conv.setFilters(parseInput(messageText));
        completeTrack(chatId, conv, conv.getTags(), conv.getFilters());
    }

    private void waitingForTagsScript(Long chatId, String messageText, Conversation conv) {
        conv.setTags(parseInput(messageText));
        conv.setState(State.WAITING_FOR_FILTERS);
        telegramClient.sendMessage(chatId, "Введите фильтры (через пробел, опционально):");
    }

    private void askAbtTagScript(Long chatId, String messageText, Conversation conv) {
        if (messageText.trim().equalsIgnoreCase("нет")) {
            completeTrack(chatId, conv, Collections.emptyList(), Collections.emptyList());
        } else if (messageText.trim().equalsIgnoreCase("да")) {
            conv.setState(State.WAITING_FOR_TAGS);
            telegramClient.sendMessage(chatId, "Введите теги (через пробел, опционально):");
        } else {
            telegramClient.sendMessage(chatId, "Пожалуйста, ответьте 'да' или 'нет'.");
        }
    }

    private void addLinkScript(Long chatId, String messageText, Conversation conv) {
        // Пользователь вводит ссылку
        conv.setLink(messageText.trim());
        conv.setState(State.ASK_ABT_TAG);
        telegramClient.sendMessage(chatId, "Хотите настроить теги и фильтры? (да/нет)");
    }

    private void startScript(Long chatId, String messageText, Conversation conv) {
        if (messageText.trim().equalsIgnoreCase(START)) {
            conv.setState(State.COMMAND_WAITING);
            commandHandlers.get(START).handle(chatId, messageText);
        } else {
            telegramClient.sendMessage(chatId, "Введите /start для начала работы с ботом.");
        }
    }

    private void processCommand(Long chatId, String messageText, Conversation conv) {
        String lower = messageText.trim().toLowerCase();
        if (lower.startsWith("/track")) {
            String[] parts = messageText.split(" ", 2);
            if (parts.length >= 2 && !parts[1].isBlank()) {
                addLinkScript(chatId, parts[1], conv);
            } else {
                conv.setState(State.ADD_LINK);
                telegramClient.sendMessage(chatId, "Введите ссылку для отслеживания:");
            }
        } else if (lower.startsWith("/help") || lower.startsWith("/list") || lower.startsWith("/untrack")) {
            commandHandlers.get(lower.split(" ")[0]).handle(chatId, messageText);
            conv.reset();
        } else {
            telegramClient.sendMessage(chatId, "Неизвестная команда. Введите /help для списка команд.");
            logger.info("Неизвестная команда: ", StructuredArguments.keyValue("message", messageText),
                StructuredArguments.keyValue(CHAT_ID, chatId));
        }
    }

    private void completeTrack(Long chatId, Conversation conv, List<String> tags, List<String> filters) {
        String link = conv.getLink();
        Mono<LinkResponse> result = scrapperClient.addLink(chatId, link, tags, filters);
        result.subscribe(
            response -> telegramClient.sendMessage(chatId, "Ссылка успешно добавлена: " + response.getUrl()),
            error -> sendErrorInfo(chatId, error)
        );
        conv.reset();
    }

    private void sendErrorInfo(Long chatId, Throwable error) {
        if (error instanceof ApiError) {
            // Если ошибка является экземпляром ApiError, выводим описание
            ApiError apiError = (ApiError) error;
            telegramClient.sendMessage(chatId, SET_LINK_ERROR + apiError.getDescription());
        } else {
            // В случае других ошибок выводим стандартное сообщение
            telegramClient.sendMessage(chatId, SET_LINK_ERROR + error.getMessage());
        }
    }

    private List<String> parseInput(String input) {
        return Arrays.stream(input.trim().split("\\s+"))
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }

    public void notifyUser(Long chatId, LinkUpdate update) {
        String notificationMessage = "Обновление по ссылке: " + update.getUrl() + "\n " + update.getDescription();
        conversations.get(chatId).reset(); //не понятно нужно ли
        telegramClient.sendMessage(chatId, "Уведомление:\n" + notificationMessage);
        logger.info("Notify user: ", StructuredArguments.keyValue(CHAT_ID, chatId),
            StructuredArguments.keyValue("notificationMessage", notificationMessage));
    }
}

