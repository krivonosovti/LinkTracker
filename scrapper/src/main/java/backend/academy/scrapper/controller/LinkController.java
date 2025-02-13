package backend.academy.scrapper.controller;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.dto.ListLinksResponse;
import backend.academy.scrapper.dto.RemoveLinkRequest;
import backend.academy.scrapper.repository.InMemoryLinkRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
        // Передаём в репозиторий URL, теги и фильтры для регистрации ссылки.
        LinkResponse response = repository.addLink(chatId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Получает список отслеживаемых ссылок для указанного чата.
     * Принимает заголовок "Tg-Chat-Id".
     * Возвращает объект ListLinksResponse.
     */
    @GetMapping
    public ResponseEntity<ListLinksResponse> getLinks(@RequestHeader("Tg-Chat-Id") Long chatId) {
        // Получаем список ссылок (предполагается, что репозиторий возвращает List<LinkResponse>)
        Set<LinkResponse> links = repository.getLinks(chatId);
        ListLinksResponse response = new ListLinksResponse(new ArrayList<>(links), links.size());
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
        repository.removeLink(chatId, request.getLink());
        return ResponseEntity.ok(new LinkResponse(chatId,request.getLink()));
    }
}
