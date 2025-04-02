package backend.academy.scrapper.entity;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
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
@Entity
@Table(name = "chats")
public class Chat {

    @Id
    @SequenceGenerator(name = "chats_id_seq", sequenceName = "chats_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chats_id_seq")
    @Column(name = "id")
    private Long id;

    @Column(name = "chat_id")
    private Long chatId;

    @Builder.Default
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(name = "chats_links",
        joinColumns = @JoinColumn(name = "chat_id"),
        inverseJoinColumns = @JoinColumn(name = "link_id"))
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

    public Long getId() {
        return id;
    }

    public Long getChatId() {
        return chatId;
    }

    public Set<Link> getLinks() {
        return links;
    }
}
