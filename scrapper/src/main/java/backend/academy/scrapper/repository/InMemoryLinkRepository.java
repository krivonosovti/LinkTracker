package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.LinkEntry;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryLinkRepository {

    private final Map<String, LinkEntry> links = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public LinkEntry addOrUpdateLink(String url, java.util.List<String> tags, java.util.List<String> filters, Long tgChatId) {
        return links.compute(url, (key, existing) -> {
            if (existing == null) {
                LinkEntry newEntry = new LinkEntry(idGenerator.getAndIncrement(), url, tags, filters);
                newEntry.addChat(tgChatId);
                return newEntry;
            } else {
                existing.addChat(tgChatId);
                return existing;
            }
        });
    }

    public LinkEntry removeChatFromLink(String url, Long tgChatId) {
        LinkEntry entry = links.get(url);
        if (entry != null) {
            entry.removeChat(tgChatId);
            if (entry.getTgChatIds().isEmpty()) {
                links.remove(url);
            }
        }
        return entry;
    }

    public Collection<LinkEntry> getAllLinks() {
        return links.values();
    }

    public void updateLink(LinkEntry linkEntry) {
        links.put(linkEntry.getUrl(), linkEntry);
    }
}
