package backend.academy.scrapper.repository.jdbc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import backend.academy.scrapper.configuration.DatabaseAccessConfig;
import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.enums.LinkStatus;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.jdbc.mapper.LinkMapper;
import backend.academy.scrapper.repository.jdbc.mapper.LinkWithChatsResultSetExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@ConditionalOnBean(DatabaseAccessConfig.JdbcAccessConfig.class)
@Repository
public class JdbcLinkRepository implements LinkRepository {

    private static final String SAVE_LINK = """
        INSERT INTO links(link_type, url, checked_at, status)
        VALUES (?, ?, ?, ?)
        RETURNING id, link_type, url, checked_at, status
        """;
    private static final String FIND_LINK_BY_URL = """
        SELECT id, link_type, url, checked_at, status FROM links WHERE url = ?
        """;
    private static final String FIND_LINKS_BY_CHAT_ID = """
        SELECT l.id, l.link_type, l.url, l.checked_at, l.status
        FROM links l
        JOIN chats_links cl ON l.id = cl.link_id
        WHERE cl.chat_id = ?
        """;
    private static final String FIND_LINKS_OLDER_THAN = """
        SELECT l.id, l.link_type, l.url, l.checked_at, l.status,
            c.id AS c_id, c.chat_id AS chat_id
        FROM links l
        JOIN chats_links cl ON l.id = cl.link_id
        JOIN chats c ON cl.chat_id = c.id
        WHERE l.status = ? AND l.checked_at < ?
        LIMIT ?
        """;
    private static final String UPDATE_LINK_STATUS = """
        UPDATE links SET status = ? WHERE id = ?
        """;
    private static final String UPDATE_CHECKED_AT = """
        UPDATE links SET checked_at = ? WHERE id = ?
        """;
    private final LinkMapper linkMapper;
    private final LinkWithChatsResultSetExtractor linkResultExtractor;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Link save(Link link) {
        return jdbcTemplate.queryForObject(SAVE_LINK, linkMapper,
            link.linkType().ordinal(),
            link.url(),
            link.checkedAt(),
            link.status().ordinal()
        );
    }

    @Override
    public List<Link> findAllByChat(Chat chat) {
        return jdbcTemplate.query(FIND_LINKS_BY_CHAT_ID, linkMapper, chat.id());
    }

    @Override
    public Optional<Link> findByUrl(String url) {
        return jdbcTemplate.queryForStream(FIND_LINK_BY_URL, linkMapper, url).findFirst();
    }

    @Override
    public boolean updateStatus(Link link, LinkStatus status) {
        return jdbcTemplate.update(UPDATE_LINK_STATUS, status.ordinal(), link.id()) > 0;
    }

    @Override
    public boolean updateCheckedAt(Link link, OffsetDateTime checkedAt) {
        return jdbcTemplate.update(UPDATE_CHECKED_AT, checkedAt, link.id()) > 0;
    }

    @Override
    public List<Link> findAllWithStatusAndOlderThan(LinkStatus status, OffsetDateTime checkedAt, Integer limit) {
        return jdbcTemplate.query(FIND_LINKS_OLDER_THAN, linkResultExtractor, status.ordinal(), checkedAt, limit
        );
    }
}
