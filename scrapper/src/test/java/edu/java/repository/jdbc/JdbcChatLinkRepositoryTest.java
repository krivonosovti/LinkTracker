package edu.java.repository.jdbc;

import backend.academy.scrapper.configuration.DatabaseAccessConfig;
import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.repository.jdbc.JdbcChatLinkRepository;
import edu.java.integration.IntegrationTest;
import edu.java.integration.config.JdbcTestConfig;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {JdbcChatLinkRepository.class})
@ContextConfiguration(classes = {JdbcTestConfig.class, DatabaseAccessConfig.JdbcAccessConfig.class})
@TestPropertySource(properties = {"app.database-access-type=jdbc"})
class JdbcChatLinkRepositoryTest extends IntegrationTest {

    @Autowired
    private JdbcChatLinkRepository chatLinkRepository;

    private static final Chat CHAT = Chat.builder().id(1L).build();
    private static final Link LINK = Link.builder().id(1L).build();

    @Nested
    class IsLinkAddedToChatTest {

        @Test
        @Sql(scripts = {"/sql/chats/add-chat.sql", "/sql/links/add-link.sql", "/sql/chats-links/add-link-to-chat.sql"})
        @Transactional
        @Rollback
        void shouldReturnTrueWhenLinkIsAddedToChat() {
            boolean isLinkAddedToChat = chatLinkRepository.isLinkAddedToChat(CHAT, LINK);

            assertTrue(isLinkAddedToChat);
        }

        @Test
        @Transactional
        void shouldReturnFalseWhenLinkIsNotAddedToChat() {
            boolean isLinkAddedToChat = chatLinkRepository.isLinkAddedToChat(CHAT, LINK);

            assertFalse(isLinkAddedToChat);
        }
    }

    @Nested
    class AddLinkToChatTest {

        @Test
        @Sql(scripts = {"/sql/chats/add-chat.sql", "/sql/links/add-link.sql"})
        @Transactional
        @Rollback
        void shouldReturnTrueWhenLinkWasAdded() {
            boolean isAdded = chatLinkRepository.addLinkToChat(CHAT, LINK);

            assertTrue(isAdded);
        }
    }

    @Nested
    class RemoveLinkFromChatTest {

        @Test
        @Sql(scripts = {"/sql/chats/add-chat.sql", "/sql/links/add-link.sql", "/sql/chats-links/add-link-to-chat.sql"})
        @Transactional
        @Rollback
        void shouldReturnTrueWhenLinkWasRemoved() {
            boolean isRemoved = chatLinkRepository.removeLinkFromChat(CHAT, LINK);

            assertTrue(isRemoved);
        }
    }
}
