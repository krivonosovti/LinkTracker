package backend.academy.bot.service;

import backend.academy.bot.dto.error.ApiErrorResponse;
import backend.academy.bot.dto.scrapperAPI.request.AddLinkRequest;
import backend.academy.bot.dto.scrapperAPI.request.RemoveLinkRequest;
import backend.academy.bot.dto.scrapperAPI.response.LinkResponse;
import backend.academy.bot.dto.scrapperAPI.response.ListLinksResponse;
import backend.academy.bot.exception.ApiError;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Service
public class ScrapperClient {

    public static final String LINKS = "/links";
    public static final String TG_CHAT_ID = "Tg-Chat-Id";
    public static final String TG_CHAT_ID_PARAM = "/tg-chat/{id}";
    private final WebClient webClient; //faintClient

    public ScrapperClient(WebClient.Builder webClientBuilder,
                          @Value("${app.scrapper-client.api:http://localhost:8081}") String baseUrl, Resource resource) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public Mono<LinkResponse> addLink(Long tgChatId, String link, List<String> tags, List<String> filters) {
        AddLinkRequest request = new AddLinkRequest(link, tags, filters);
        // Проверяем успешный статус ответа
        return webClient.post()
            .uri(LINKS)
            .header(TG_CHAT_ID, tgChatId.toString())
            .bodyValue(request)
            .exchangeToMono(response -> getResponseMono(response, LinkResponse.class));
    }

    public Mono<LinkResponse> removeLink(Long tgChatId, String link) {
        RemoveLinkRequest request = new RemoveLinkRequest(link);
        return webClient.method(HttpMethod.DELETE)
            .uri(LINKS)
            .header(TG_CHAT_ID, tgChatId.toString())
            .bodyValue(request)
            .exchangeToMono(response -> getResponseMono(response, LinkResponse.class));
    }

    public Mono<ListLinksResponse> getLinks(Long tgChatId) {
        return webClient.get()
            .uri(LINKS)
            .header(TG_CHAT_ID, tgChatId.toString())
            .exchangeToMono(response -> getResponseMono(response, ListLinksResponse.class));
    }

    public Mono<Void> registerChat(Long tgChatId) {
        return webClient.post()
            .uri(uriBuilder -> uriBuilder.path(TG_CHAT_ID_PARAM).build(tgChatId))
            .exchangeToMono(response -> getResponseMono(response, Void.class));
    }

    public Mono<Void> deleteChat(Long tgChatId) {
        return webClient.delete()
            .uri(uriBuilder -> uriBuilder.path(TG_CHAT_ID_PARAM).build(tgChatId))
            .exchangeToMono(response -> getResponseMono(response, Void.class));
    }

    private static <T> @NotNull Mono<T> getResponseMono(ClientResponse response, Class<T> responseType) {
        if (response.statusCode().is2xxSuccessful()) {
            // Если статус 2xx, десериализуем в переданный responseType
            return response.bodyToMono(responseType);
        } else {
            // Если ошибка, десериализуем в ApiErrorResponse и выбрасываем исключение
            return response.bodyToMono(ApiErrorResponse.class)
                .flatMap(apiErrorResponse -> Mono.error(new ApiError(
                    apiErrorResponse.getDescription(),
                    apiErrorResponse.getCode(),
                    apiErrorResponse.getExceptionName(),
                    apiErrorResponse.getExceptionMessage(),
                    apiErrorResponse.getStackTrace()
                )));
        }
    }

}
