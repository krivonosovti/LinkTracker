package backend.academy.bot.command;

import backend.academy.bot.service.ScrapperClient;
import backend.academy.bot.service.TelegramClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class StartCommandHandler implements CommandHandler {

    private final ScrapperClient scrapperClient;
    private final TelegramClient telegramClient;

    public StartCommandHandler(ScrapperClient scrapperClient, TelegramClient telegramClient) {
        this.scrapperClient = scrapperClient;
        this.telegramClient = telegramClient;
    }

    @Override
    public String getCommand() {
        return "/start";
    }

    @Override
    public void handle(Long chatId, String messageText) {
        // Регистрируем чат в Scrapper-сервисе
        Mono<Void> result = scrapperClient.registerChat(chatId)
            .doOnSuccess(unused -> telegramClient.sendMessage(chatId, getWelcomeMessage()))
            .doOnError(error -> telegramClient.sendMessage(chatId, "Ошибка регистрации: " + error.getMessage()))
            .then();
        result.subscribe();
    }

    private String getWelcomeMessage() {
        return """
                👋 Привет! Я бот для отслеживания изменений на интересующих вас страницах.

                📌 Доступные команды:
                /track <ссылка> – добавить ссылку в отслеживание
                /untrack <ссылка> – удалить ссылку из списка
                /list – показать все отслеживаемые ссылки

                🔍 Просто отправь команду, и я начну следить за изменениями!
                """;
    }
}
