package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.enums.LinkStatus;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryLinkRepository implements LinkRepository {

    private final Map<String, Link> linkStorage = new HashMap<>();
    private final Map<Chat, List<Link>> chatLinksStorage = new HashMap<>();

    @Override
    public Link save(Link link) {
        linkStorage.put(link.url(), link);
        return link;
    }

    @Override
    public List<Link> findAllByChat(Chat chat) {
        return chatLinksStorage.getOrDefault(chat, new ArrayList<>());
    }

    @Override
    public Optional<Link> findByUrl(String url) {
        return Optional.ofNullable(linkStorage.get(url));
    }

    @Override
    public boolean updateStatus(Link link, LinkStatus status) {
        Link existingLink = linkStorage.get(link.url());
        if (existingLink != null) {
            existingLink.status(status);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateCheckedAt(Link link, OffsetDateTime checkedAt) {
        Link existingLink = linkStorage.get(link.url());
        if (existingLink != null) {
            existingLink.checkedAt(checkedAt);
            return true;
        }
        return false;
    }

    @Override
    public List<Link> findAllWithStatusAndOlderThan(LinkStatus status, OffsetDateTime checkedAt, Pageable pageable) {
        List<Link> result = new ArrayList<>();
        for (Link link : linkStorage.values()) {
//            if (link.status() == status && link.checkedAt().isBefore(checkedAt)) {
            if (link.status() == status) {
                result.add(link);
            }
        }
        // Paginate if needed (for simplicity, no pagination logic implemented here)
        return result;
    }
}
