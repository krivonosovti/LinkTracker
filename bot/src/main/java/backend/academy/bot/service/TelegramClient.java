package backend.academy.bot.service;

import backend.academy.bot.BotConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class TelegramClient {

    private final BotConfig botConfig;
    private final WebClient webClient;

    public TelegramClient(BotConfig botConfig, WebClient.Builder webClientBuilder) {
        this.botConfig = botConfig;
        this.webClient = webClientBuilder.baseUrl("https://api.telegram.org").build();
    }

    public void sendMessage(Long chatId, String text) {
        String token = botConfig.telegramToken();
        String url = "/bot" + token + "/sendMessage";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("chat_id", chatId);
        requestBody.put("text", text);

        webClient.post()
            .uri(url)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(String.class)
            .subscribe(
                response -> System.out.println("Сообщение успешно отправлено: " + response),
                error -> System.err.println("Ошибка при отправке сообщения: " + error.getMessage())
            );
    }
}
