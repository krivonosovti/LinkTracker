package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.LinkUpdate;
import backend.academy.scrapper.repository.InMemoryLinkRepository;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class LinkScrapperService {

    private final InMemoryLinkRepository repository;
    private final ChangeApiClient changeApiClient;
    private final BotApiClient botApiClient;
    Logger logger = Logger.getLogger(LinkScrapperService.class.getName());
    public LinkScrapperService(InMemoryLinkRepository repository,
                               ChangeApiClient changeApiClient,
                               BotApiClient botApiClient) {
        this.repository = repository;
        this.changeApiClient = changeApiClient;
        this.botApiClient = botApiClient;
    }

    // Проверяем обновления каждые 5 минут (300000 мс)
    @Scheduled(fixedDelayString = "${scrapper.check-delay-ms:60000}")
    public void checkForUpdates() {
        repository.getAllLinks().forEach(linkEntry -> {
            // Вызываем клиента для получения обновлений по URL ссылки
            changeApiClient.getUpdate(linkEntry.getUrl())
                .subscribe(update -> {
                    if (update.isUpdated(linkEntry.getLastUpdated())) {
                        // Формируем уведомление для бота
                        LinkUpdate notification = new LinkUpdate(
                            linkEntry.getId(),
                            linkEntry.getUrl(),
                            update.getDescription(),
                            List.copyOf(linkEntry.getTgChatIds())
                        );
                        botApiClient.sendUpdate(notification).subscribe(
                            response -> logger.log(Level.INFO, "Сообщение успешно отправлено" + response),
                            error -> logger.log(Level.INFO, "Сообщение успешно отправлено" + error)
                        );

                        // Обновляем дату последнего обновления и сохраняем изменения
                        linkEntry.setLastUpdated(update.getLastUpdated());
                        repository.updateLink(linkEntry);
                    }
                }, error -> {
                    System.err.println("Ошибка проверки обновлений для " + linkEntry.getUrl()
                        + ": " + error.getMessage());
                });
        });
    }
}
