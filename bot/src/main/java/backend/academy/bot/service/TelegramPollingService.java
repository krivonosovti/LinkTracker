package backend.academy.bot.service;

import backend.academy.bot.BotConfig;
import backend.academy.bot.dto.TelegramResponse;
import backend.academy.bot.dto.TelegramUpdate;
import backend.academy.bot.state.StateMachine;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class TelegramPollingService {
    private final BotConfig botConfig;
    private final WebClient webClient;
    private final StateMachine stateMachine;
    // Для хранения последнего обработанного update_id
    private long lastUpdateId = 0;

    public TelegramPollingService(BotConfig botConfig, WebClient.Builder webClientBuilder, StateMachine stateMachine) {
        this.botConfig = botConfig;
        this.stateMachine = stateMachine;
        this.webClient = webClientBuilder.baseUrl("https://api.telegram.org").build();
    }

    @Scheduled(fixedDelayString = "3000")
    public void pollUpdates() {
        String token = botConfig.telegramToken();
        // Запрашиваем обновления с offset, чтобы получать только новые
        String url = "/bot" + token + "/getUpdates?offset=" + (lastUpdateId + 1);

        TelegramResponse response = webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono(TelegramResponse.class)
            .block(); // В планировщике можно использовать block() для упрощения

        if (response != null && response.isOk() && response.getResult() != null) {
            for (TelegramUpdate update : response.getResult()) {
                // Обновляем offset, чтобы не получать старые обновления
                if (update.getUpdateId() > lastUpdateId) {
                    lastUpdateId = update.getUpdateId();
                }
                // Если сообщение не пустое, передаём его на обработку
                if (update.getMessage() != null && update.getMessage().getText() != null) {
                    Long chatId = update.getMessage().getChat().getId();
                    String text = update.getMessage().getText();
                    stateMachine.start(chatId, text);
                }
            }
        }
    }
}
