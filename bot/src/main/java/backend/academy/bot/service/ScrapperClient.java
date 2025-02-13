package backend.academy.bot.service;

import backend.academy.bot.dto.LinkResponse;
import backend.academy.bot.dto.ListLinksResponse;
import backend.academy.bot.dto.AddLinkRequest;
import backend.academy.bot.dto.RemoveLinkRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.List;

@Service
public class ScrapperClient {

    private final WebClient webClient;

    public ScrapperClient(WebClient.Builder webClientBuilder,
                          @Value("${scrapper.base-url:http://localhost:8081}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    /**
     * Добавляет ссылку для отслеживания.
     * Соответствует POST /links с заголовком Tg-Chat-Id и телом запроса, описанным в AddLinkRequest.
     * Возвращает объект LinkResponse.
     */
    public Mono<LinkResponse> addLink(Long tgChatId, String link, List<String> tags, List<String> filters) {
        AddLinkRequest request = new AddLinkRequest(link, tags, filters);
        return webClient.post()
            .uri("/links")
            .header("Tg-Chat-Id", tgChatId.toString())
            .bodyValue(request)
            .retrieve()
            .bodyToMono(LinkResponse.class);
    }

    /**
     * Удаляет отслеживаемую ссылку.
     * Соответствует DELETE /links с заголовком Tg-Chat-Id и телом запроса, описанным в RemoveLinkRequest.
     * Возвращает объект LinkResponse.
     */
    public Mono<LinkResponse> removeLink(Long tgChatId, String link) {
        RemoveLinkRequest request = new RemoveLinkRequest(link);
        return webClient.method(HttpMethod.DELETE)
            .uri("/links")
            .header("Tg-Chat-Id", tgChatId.toString())
            .bodyValue(request)
            .retrieve()
            .bodyToMono(LinkResponse.class);
    }

    /**
     * Получает список отслеживаемых ссылок для указанного чата.
     * Соответствует GET /links с заголовком Tg-Chat-Id.
     * Возвращает объект ListLinksResponse.
     */
    public Mono<ListLinksResponse> getLinks(Long tgChatId) {
        return webClient.get()
            .uri("/links")
            .header("Tg-Chat-Id", tgChatId.toString())
            .retrieve()
            .bodyToMono(ListLinksResponse.class);
    }

    /**
     * Регистрирует чат.
     * Соответствует POST /tg-chat/{id} без тела запроса.
     * Ожидается статус 200, без возвращаемого объекта.
     */
    public Mono<Void> registerChat(Long tgChatId) {
        return webClient.post()
            .uri(uriBuilder -> uriBuilder.path("/tg-chat/{id}").build(tgChatId))
            .retrieve()
            .bodyToMono(Void.class);
    }

    /**
     * Удаляет чат.
     * Соответствует DELETE /tg-chat/{id} без тела запроса.
     * Ожидается статус 200, без возвращаемого объекта.
     */
    public Mono<Void> deleteChat(Long tgChatId) {
        return webClient.delete()
            .uri(uriBuilder -> uriBuilder.path("/tg-chat/{id}").build(tgChatId))
            .retrieve()
            .bodyToMono(Void.class);
    }
}
