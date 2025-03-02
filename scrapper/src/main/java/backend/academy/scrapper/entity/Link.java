package backend.academy.scrapper.entity;

import backend.academy.scrapper.enums.LinkStatus;
import backend.academy.scrapper.enums.LinkType;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Objects;
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
public class Link {

    private Long id;

    private LinkType linkType;

    private String url;

    @Builder.Default
    private OffsetDateTime checkedAt = OffsetDateTime.now();

    @Builder.Default
    private LinkStatus status = LinkStatus.ACTIVE;

    @Builder.Default
    private Set<Chat> chats = new HashSet<>();

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Link link)) {
            return false;
        }
        return id != null && Objects.equals(id, link.id());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
