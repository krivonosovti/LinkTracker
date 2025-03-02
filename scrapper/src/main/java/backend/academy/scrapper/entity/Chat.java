package backend.academy.scrapper.entity;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Chat {
    private Long id;
    private Long chatId;

    @Builder.Default
    private Set<Link> links = new HashSet<>();

    public void addLink(Link link) {
        links.add(link);
        link.chats().add(this);
    }

    public void removeLink(Link link) {
        links.remove(link);
        link.chats().remove(this);
    }

    public Optional<Link> findLinkByUrl(String url) {
        return links.stream()
            .filter(it -> it.url().equals(url))
            .findFirst();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Chat chat)) {
            return false;
        }
        return id != null && Objects.equals(id, chat.id());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
