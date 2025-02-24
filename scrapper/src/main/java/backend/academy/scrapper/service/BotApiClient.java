package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.LinkUpdate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class BotApiClient {
    private final WebClient webClient;

    public BotApiClient(WebClient.Builder webClientBuilder,
                        @Value("${bot.base-url}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public Mono<?> sendUpdate(LinkUpdate update) {   // продумать как обратботать ответ apiError
        return webClient.post()
            .uri("/updates")
            .bodyValue(update)
            .retrieve()
            .bodyToMono(Void.class);
    }
}
