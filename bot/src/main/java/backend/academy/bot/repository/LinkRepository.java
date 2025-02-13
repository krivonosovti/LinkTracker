package backend.academy.bot.repository;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class LinkRepository {
    // Хранит для каждого chatId список ссылок
    private final ConcurrentHashMap<Long, List<String>> storage = new ConcurrentHashMap<>();

    /**
     * Добавляет ссылку для указанного chatId.
     */
    public void addLink(Long chatId, String link) {
        storage.compute(chatId, (id, links) -> {
            if (links == null) {
                links = new ArrayList<>();
            }
            links.add(link);
            return links;
        });
    }

    /**
     * Возвращает список ссылок для chatId.
     */
    public List<String> getLinks(Long chatId) {
        return storage.getOrDefault(chatId, Collections.emptyList());
    }

    /**
     * Удаляет ссылку для chatId.
     */
    public boolean removeLink(Long chatId, String link) {
        List<String> links = storage.get(chatId);
        if (links != null) {
            boolean removed = links.remove(link);
            if (links.isEmpty()) {
                storage.remove(chatId);
            }
            return removed;
        }
        return false;
    }
}
