package backend.academy.bot.command;

import backend.academy.bot.dto.ListLinksResponse;
import backend.academy.bot.service.ScrapperClient;
import backend.academy.bot.service.TelegramClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.util.logging.Logger;

@Component
public class ListCommandHandler implements CommandHandler {

    private final ScrapperClient scrapperClient;
    private final TelegramClient telegramClient;

    public ListCommandHandler(ScrapperClient scrapperClient, TelegramClient telegramClient) {
        this.scrapperClient = scrapperClient;
        this.telegramClient = telegramClient;
    }

    @Override
    public String getCommand() {
        return "/list";
    }

    @Override
    public void handle(Long chatId, String messageText) {
        Mono<ListLinksResponse> monoResponse = scrapperClient.getLinks(chatId);
        monoResponse.subscribe(
            response -> {
                if (response.getLinks() == null || response.getLinks().isEmpty()) {
                    telegramClient.sendMessage(chatId, "Список отслеживаемых ссылок пуст.");
                } else {
                    StringBuilder sb = new StringBuilder("Отслеживаемые ссылки:\n");
                    response.getLinks().forEach(link -> sb.append(link.getUrl()).append("\n"));
                    telegramClient.sendMessage(chatId, sb.toString());
                }
            },
            error -> telegramClient.sendMessage(chatId, "Ошибка получения списка ссылок: " + error.getMessage())
        );
    }
}
