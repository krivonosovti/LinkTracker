//package backend.academy.scrapper.repository;
//
//import backend.academy.scrapper.model.LinkEntry;
//import org.springframework.stereotype.Repository;
//import java.util.Collection;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.atomic.AtomicLong;
//
//@Repository
//public class InMemoryLinkRepository {
//
//    private final Map<String, LinkEntry> links = new ConcurrentHashMap<>();
//    private final AtomicLong idGenerator = new AtomicLong(1);
//
//    public LinkEntry addOrUpdateLink(String url,
//                                     java.util.List<String> tags,
//                                     java.util.List<String> filters,
//                                     Long tgChatId) {
//        return links.compute(url, (key, existing) -> {
//            if (existing == null) {
//                LinkEntry newEntry = new LinkEntry(idGenerator.getAndIncrement(), url, tags, filters);
//                newEntry.addChat(tgChatId);
//                return newEntry;
//            } else {
//                existing.addChat(tgChatId);
//                return existing;
//            }
//        });
//    }
//
//    public LinkEntry removeChatFromLink(String url, Long tgChatId) {
//        LinkEntry entry = links.get(url);
//        if (entry != null) {
//            entry.removeChat(tgChatId);
//            if (entry.getTgChatIds().isEmpty()) {
//                links.remove(url);
//            }
//        }
//        return entry;
//    }
//
//    public Collection<LinkEntry> getAllLinks() {
//        return links.values();
//    }
//}
package backend.academy.scrapper.repository;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.dto.LinkResponse;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryLinkRepository {
    private final Map<Long, Set<LinkResponse>> userLinks = new ConcurrentHashMap<>();

    public LinkResponse addLink(Long chatId, AddLinkRequest link) {
        LinkResponse response = new LinkResponse(chatId, link);
        userLinks.computeIfAbsent(chatId, k -> Collections.synchronizedSet(new HashSet<>())).add(response);
        return response;
    }

    public LinkResponse removeLink(Long chatId, String link) {  //сломано удаление
        LinkResponse response = new LinkResponse(chatId, link);

        // Используем computeIfPresent для модификации Set, если он существует
        userLinks.computeIfPresent(chatId, (id, links) -> {
            links.remove(response);
            return links.isEmpty() ? null : links;  // Если Set пустой, удаляем его из Map
        });

        return response;
    }

    public Set<LinkResponse> getLinks(Long chatId) {
        return userLinks.getOrDefault(chatId, Collections.emptySet());
    }
}
