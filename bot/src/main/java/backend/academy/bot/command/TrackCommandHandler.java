package backend.academy.bot.command;

import backend.academy.bot.service.ScrapperClient;
import backend.academy.bot.service.TelegramClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.util.Collections;

@Component
public class TrackCommandHandler implements CommandHandler {

    private final ScrapperClient scrapperClient;
    private final TelegramClient telegramClient;

    public TrackCommandHandler(ScrapperClient scrapperClient, TelegramClient telegramClient) {
        this.scrapperClient = scrapperClient;
        this.telegramClient = telegramClient;
    }

    @Override
    public String getCommand() {
        return "/track";
    }

    @Override
    public void handle(Long chatId, String messageText) {
        // Ожидается, что сообщение выглядит как "/track <ссылка>"
        String[] parts = messageText.split(" ");
        if (parts.length < 2) {
            telegramClient.sendMessage(chatId, "Использование: /track <ссылка>");
            return;
        }
        String link = parts[1];

        Mono<Void> result = scrapperClient.addLink(chatId, link, Collections.emptyList(), Collections.emptyList())
            .doOnNext(linkResponse ->
                telegramClient.sendMessage(chatId, "Ссылка успешно добавлена: " + linkResponse.getUrl()))
            .doOnError(error ->
                telegramClient.sendMessage(chatId, "Ошибка при добавлении ссылки: " + error.getMessage()))
            .then();
        result.subscribe();
    }
}
