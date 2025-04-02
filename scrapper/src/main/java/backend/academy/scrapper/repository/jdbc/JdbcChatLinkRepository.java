package backend.academy.scrapper.repository.jdbc;


import java.util.Optional;
import backend.academy.scrapper.configuration.DatabaseAccessConfig;
import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.repository.ChatLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@ConditionalOnBean(DatabaseAccessConfig.JdbcAccessConfig.class)
@Repository
public class JdbcChatLinkRepository implements ChatLinkRepository {

    private static final String IS_LINK_ADDED_TO_CHAT = """
        SELECT count(*) FROM chats_links WHERE chat_id = ? AND link_id = ?
        """;
    private static final String ADD_LINK_TO_CHAT = """
        INSERT INTO chats_links(chat_id, link_id) VALUES (?, ?)
        """;
    private static final String REMOVE_LINK_FROM_CHAT = """
        DELETE FROM chats_links WHERE chat_id = ? AND link_id = ?
        """;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public boolean isLinkAddedToChat(Chat chat, Link link) {
        return Optional.ofNullable(jdbcTemplate.queryForObject(
                IS_LINK_ADDED_TO_CHAT,
                Integer.class,
                chat.id(),
                link.id()
            ))
            .map(it -> it > 0)
            .orElse(false);
    }

    @Override
    public boolean addLinkToChat(Chat chat, Link link) {
        return jdbcTemplate.update(ADD_LINK_TO_CHAT, chat.id(), link.id()) > 0;
    }

    @Override
    public boolean removeLinkFromChat(Chat chat, Link link) {
        return jdbcTemplate.update(REMOVE_LINK_FROM_CHAT, chat.id(), link.id()) > 0;
    }
}
