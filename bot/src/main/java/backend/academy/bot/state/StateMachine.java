package backend.academy.bot.state;

import backend.academy.bot.command.CommandHandler;
import backend.academy.bot.dto.LinkResponse;
import backend.academy.bot.service.TelegramClient;
import backend.academy.bot.service.ScrapperClient;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import net.logstash.logback.argument.StructuredArguments;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class StateMachine {
    // Хранение разговоров для каждого пользователя (chatId)
    private final Map<Long, Conversation> conversations = new ConcurrentHashMap<>();
    private final TelegramClient telegramClient;
    private final ScrapperClient scrapperClient;
    private static final Logger logger = LoggerFactory.getLogger(StateMachine.class);
    private final Map<String, CommandHandler> commandHandlers = new HashMap<>();

    public StateMachine(TelegramClient telegramClient, ScrapperClient scrapperClient, List<CommandHandler> handlers) {
        this.telegramClient = telegramClient;
        this.scrapperClient = scrapperClient;
        for (CommandHandler handler : handlers) {
            commandHandlers.put(handler.getCommand().toLowerCase(), handler);
        }
    }

    // Точка входа: вызывается из TelegramPollingService
    public void start(Long chatId, String messageText) {
        Conversation conv = conversations.computeIfAbsent(chatId, id -> new Conversation());
        State state = conv.getState();
            // подумать куда именно точку входа делать, так он будет просить каждый раз при включении микросевиса требовать старт
        if (state == State.START) {
            // В состоянии START ожидаем только /start
            if (messageText.trim().equalsIgnoreCase("/start")) {
                conv.setState(State.COMMAND_WAITING);
                telegramClient.sendMessage(chatId, "Бот активирован! Введите /help для списка команд.");
            } else {
                telegramClient.sendMessage(chatId, "Введите /start для начала работы с ботом.");
            }
            return;
        }

        if (state == State.COMMAND_WAITING) {
            processCommand(chatId, messageText, conv);
            return;
        }

        // Обработка состояний, связанных с командой /track
        switch (state) {
            case ADD_LINK:
                // Пользователь вводит ссылку
                conv.setLink(messageText.trim());
                conv.setState(State.ASK_ABT_TAG);
                telegramClient.sendMessage(chatId, "Хотите настроить теги и фильтры? (да/нет)");
                break;
            case ASK_ABT_TAG:
                if (messageText.trim().equalsIgnoreCase("нет")) {
                    completeTrack(chatId, conv, Collections.emptyList(), Collections.emptyList());
                } else if (messageText.trim().equalsIgnoreCase("да")) {
                    conv.setState(State.WAITING_FOR_TAGS);
                    telegramClient.sendMessage(chatId, "Введите теги (через пробел, опционально):");
                } else {
                    telegramClient.sendMessage(chatId, "Пожалуйста, ответьте 'да' или 'нет'.");
                }
                break;
            case WAITING_FOR_TAGS:
                conv.setTags(parseInput(messageText));
                conv.setState(State.WAITING_FOR_FILTERS);
                telegramClient.sendMessage(chatId, "Введите фильтры (через пробел, опционально):");
                break;
            case WAITING_FOR_FILTERS:
                conv.setFilters(parseInput(messageText));
                completeTrack(chatId, conv, conv.getTags(), conv.getFilters());
                break;
            default:
                telegramClient.sendMessage(chatId, "Неверное состояние. Введите /help для получения списка команд.");
                conv.reset();
                logger.error("Недопустимое состояние", StructuredArguments.keyValue("chatId", chatId));
        }
    }

    // Обработка команд в состоянии COMMAND_WAITING
    private void processCommand(Long chatId, String messageText, Conversation conv) {
        String lower = messageText.trim().toLowerCase();
        if (lower.startsWith("/track")) {
            // Если команда /track передана с параметром (ссылка)
            String[] parts = messageText.split(" ", 2);
            if (parts.length >= 2 && !parts[1].isBlank()) {
                conv.setLink(parts[1].trim());
                conv.setState(State.ASK_ABT_TAG);
                telegramClient.sendMessage(chatId, "Хотите настроить теги и фильтры? (да/нет)");
            } else {
                // Если ссылки нет, переходим в ADD_LINK
                conv.setState(State.ADD_LINK);
                telegramClient.sendMessage(chatId, "Введите ссылку для отслеживания:");
            }
        } else if (lower.startsWith("/help") || lower.startsWith("/list") || lower.startsWith("/untrack")) {
            commandHandlers.get(lower.split(" ")[0]).handle(chatId, messageText);
            conv.reset();
        } else {
            telegramClient.sendMessage(chatId, "Неизвестная команда. Введите /help для списка команд.");
            logger.warn("Неизвестная команда: ", StructuredArguments.keyValue("message", messageText),
                StructuredArguments.keyValue("chatId", chatId));
        }
    }

    // Завершение команды /track: вызываем ScrapperClient и возвращаем результат
    private void completeTrack(Long chatId, Conversation conv, List<String> tags, List<String> filters) {
        String link = conv.getLink();
        Mono<LinkResponse> result = scrapperClient.addLink(chatId, link, tags, filters);
        result.subscribe(
            response -> telegramClient.sendMessage(chatId, "Ссылка успешно добавлена: " + response.getUrl()),
            error -> telegramClient.sendMessage(chatId, "Ошибка при добавлении ссылки: " + error.getMessage())
        );
        conv.reset();
    }

    private List<String> parseInput(String input) {
        return Arrays.stream(input.trim().split("\\s+"))
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }

    // Новый метод: точка входа для уведомлений от Scrapper-сервиса
    public void notifyUser(Long chatId, String notificationMessage) {
        logger.info("Notify user: ", StructuredArguments.keyValue("chatId", chatId), StructuredArguments.keyValue("notificationMessage", notificationMessage));
        telegramClient.sendMessage(chatId, "Уведомление: " + notificationMessage);
    }
}

