package backend.academy.bot.command;

import backend.academy.bot.dto.scrapperAPI.response.LinkResponse;
import backend.academy.bot.exception.ApiError;
import backend.academy.bot.service.ScrapperClient;
import backend.academy.bot.service.TelegramClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class UntrackCommandHandler implements CommandHandler {

    private final ScrapperClient scrapperClient;
    private final TelegramClient telegramClient;

    public UntrackCommandHandler(ScrapperClient scrapperClient, TelegramClient telegramClient) {
        this.scrapperClient = scrapperClient;
        this.telegramClient = telegramClient;
    }

    @Override
    public String getCommand() {
        return "/untrack";
    }

    @Override
    public void handle(Long chatId, String messageText) {
        // Ожидается, что сообщение выглядит как "/untrack <ссылка>"
        String[] parts = messageText.split(" ");
        if (parts.length < 2) {
            telegramClient.sendMessage(chatId, "Использование: /untrack <ссылка>");
            return;
        }
        String link = parts[1];
        Mono<LinkResponse> result = scrapperClient.removeLink(chatId, link);
        result.subscribe(
            response -> telegramClient.sendMessage(chatId, "Ссылка успешно удалена: " + response.getUrl()),
            error -> sendErrorInfo(chatId, error, "Ошибка удаления ссылки")
            );
    }

    private void sendErrorInfo(Long chatId, Throwable error, String msg) {
        if (error instanceof ApiError) {
            // Если ошибка является экземпляром ApiError, выводим описание
            ApiError apiError = (ApiError) error;
            telegramClient.sendMessage(chatId, msg + ":\n " + apiError.getDescription());
        } else {
            // В случае других ошибок выводим стандартное сообщение
            telegramClient.sendMessage(chatId, msg + ": \n " + error.getMessage());
        }
    }
}
