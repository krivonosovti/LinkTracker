package backend.academy.bot.service;

import backend.academy.bot.BotConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

@Service
public class TelegramClient {

    private final BotConfig botConfig;
    private final WebClient webClient;
    private static final Logger logger = LoggerFactory.getLogger(TelegramClient.class);

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
                response -> logger.info("Сообщение успешно отправлено",
                    StructuredArguments.keyValue("response", response)),
                error -> logger.error("Ошибка при отправке сообщения",
                    StructuredArguments.keyValue("error", error.getMessage()))
            );
    }
}
