package backend.academy.scrapper.controller;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.dto.ListLinksResponse;
import backend.academy.scrapper.dto.RemoveLinkRequest;
import backend.academy.scrapper.model.LinkEntry;
import backend.academy.scrapper.repository.InMemoryLinkRepository;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/links")
public class LinkController {

    private final InMemoryLinkRepository repository;

    public LinkController(InMemoryLinkRepository repository) {
        this.repository = repository;
    }

    /**
     * Добавляет отслеживаемую ссылку.
     * Принимает заголовок "Tg-Chat-Id" и тело запроса типа AddLinkRequest.
     * Возвращает объект LinkResponse.
     */
    @PostMapping
    public ResponseEntity<LinkResponse> addLink(
        @RequestHeader("Tg-Chat-Id") Long chatId,
        @RequestBody AddLinkRequest request
    ) {
        // Добавляем или обновляем ссылку в репозитории
        LinkEntry entry = repository.addOrUpdateLink(request.getLink(), request.getTags(),
            request.getFilters(), chatId);
        LinkResponse response = toLinkResponse(entry);
        return ResponseEntity.ok(response);
    }

    /**
     * Получает список отслеживаемых ссылок для указанного чата.
     * Принимает заголовок "Tg-Chat-Id".
     * Возвращает объект ListLinksResponse.
     */
    @GetMapping
    public ResponseEntity<ListLinksResponse> getLinks(@RequestHeader("Tg-Chat-Id") Long chatId) {
        Collection<LinkEntry> allEntries = repository.getAllLinks();
        // Фильтруем записи, оставляя только те, где присутствует данный chatId
        List<LinkResponse> responses = allEntries.stream()
            .filter(entry -> entry.getTgChatIds().contains(chatId))
            .map(this::toLinkResponse)
            .collect(Collectors.toList());
        ListLinksResponse response = new ListLinksResponse(responses, responses.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Убирает отслеживание ссылки.
     * Принимает заголовок "Tg-Chat-Id" и тело запроса типа RemoveLinkRequest.
     * Возвращает объект LinkResponse.
     */
    @DeleteMapping
    public ResponseEntity<LinkResponse> removeLink(
        @RequestHeader("Tg-Chat-Id") Long chatId,
        @RequestBody RemoveLinkRequest request
    ) {
        // Убираем chatId из списка отслеживающих для ссылки
        LinkEntry entry = repository.removeChatFromLink(request.getLink(), chatId);
        // Если запись найдена, возвращаем актуальное состояние, иначе формируем DTO с минимальными данными
        LinkResponse response = (entry != null) ? toLinkResponse(entry)
            : new LinkResponse(null, request.getLink(), null, null);
        return ResponseEntity.ok(response);
    }

    /**
     * Преобразует внутреннюю модель LinkEntry в DTO LinkResponse.
     */
    private LinkResponse toLinkResponse(LinkEntry entry) {
        return new LinkResponse(entry.getId(), entry.getUrl(), entry.getTags(), entry.getFilters());
    }
}
