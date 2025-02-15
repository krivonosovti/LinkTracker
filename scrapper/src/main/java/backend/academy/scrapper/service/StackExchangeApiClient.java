package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.ExternalResourceUpdate;
import backend.academy.scrapper.dto.StackExchangeQuestionResponse;
import backend.academy.scrapper.model.StackExchangeQuestionItem;
import java.net.URI;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Service
public class StackExchangeApiClient implements ChangeApiClient {

    private final WebClient webClient;
    // Регулярное выражение для извлечения идентификатора вопроса из URL,
    // например, из пути "/questions/292357/..."
    private static final Pattern QUESTION_PATTERN = Pattern.compile("/questions/(\\d+)");

    public StackExchangeApiClient(WebClient.Builder webClientBuilder,
                                  @Value("${stackexchange.base-url:https://api.stackexchange.com/2.3}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public Mono<ExternalResourceUpdate> getUpdate(String url) {
        Long questionId = extractQuestionId(url);
        if (questionId == null) {
            return Mono.error(new IllegalArgumentException("Некорректный URL для StackExchange: " + url));
        }
        return webClient.get()
            .uri(uriBuilder -> uriBuilder.path("/questions/{id}")
                .queryParam("order", "desc")
                .queryParam("sort", "activity")
                .queryParam("site", "stackoverflow")
                .build(questionId))
            .retrieve()
            .bodyToMono(StackExchangeQuestionResponse.class)
            .flatMap(response -> {
                if (response.getItems() == null || response.getItems().isEmpty()) {
                    return Mono.error(new IllegalStateException("Нет данных по вопросу " + questionId));
                }
                StackExchangeQuestionItem item = response.getItems().get(0);
                Instant lastUpdated = Instant.ofEpochSecond(item.getLast_activity_date());
                String description = item.getTitle();
                ExternalResourceUpdate update = new ExternalResourceUpdate(url, description, lastUpdated);
                return Mono.just(update);
            });
    }

    /**
     * Извлекает идентификатор вопроса из URL.
     * Ожидается, что URL имеет формат "https://stackoverflow.com/questions/{id}/..."
     */
    private Long extractQuestionId(String url) {
        try {
            URI uri = URI.create(url);
            Matcher matcher = QUESTION_PATTERN.matcher(uri.getPath());
            if (matcher.find()) {
                return Long.valueOf(matcher.group(1));
            }
        } catch (Exception e) {
            // можно добавить логирование ошибки
        }
        return null;
    }
}
