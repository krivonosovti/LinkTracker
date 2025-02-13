package backend.academy.bot.command;

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
        Mono<Void> result = scrapperClient.removeLink(chatId, link)
            .doOnNext(linkResponse ->
                telegramClient.sendMessage(chatId, "Ссылка успешно удалена: " + linkResponse.getUrl()))
            .doOnError(error ->
                telegramClient.sendMessage(chatId, "Ошибка при удалении ссылки: " + error.getMessage()))
            .then();
        result.subscribe();
    }
}
