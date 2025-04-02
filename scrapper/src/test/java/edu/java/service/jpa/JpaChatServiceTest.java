package edu.java.service.jpa;

import java.util.List;
import backend.academy.scrapper.configuration.DatabaseAccessConfig;
import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.exception.ApiException;
import backend.academy.scrapper.repository.jpa.JpaChatRepository;
import backend.academy.scrapper.service.jpa.JpaChatService;
import edu.java.integration.IntegrationTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({JpaChatService.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {"app.database-access-type=jpa"})
@DataJpaTest(includeFilters = @ComponentScan.Filter(
    type = FilterType.ASSIGNABLE_TYPE, classes = DatabaseAccessConfig.JpaAccessConfig.class))
class JpaChatServiceTest extends IntegrationTest {

    @Autowired
    private JpaChatService chatService;
    @Autowired
    private JpaChatRepository chatRepository;

    @Nested
    class FindByChatIdTest {

        @Test
        @Sql(scripts = {"/sql/chats/add-chat.sql", "/sql/links/add-links-list.sql",
            "/sql/chats-links/add-links-list-to-chat.sql"})
        @Transactional
        @Rollback
        void shouldReturnChatWhenChatExists() {
            Chat chat = chatService.findByChatId(123L);

            assertThat(chat).isNotNull();
            assertThat(chat.id()).isEqualTo(1);
            assertThat(chat.links().size()).isEqualTo(2);
            assertThat(chat.links().stream().map(Link::id).toList())
                .containsExactlyInAnyOrderElementsOf(List.of(1L, 2L));
        }

        @Test
        @Transactional
        void shouldThrowExceptionWhenChatNotFound() {
            ApiException ex = catchThrowableOfType(
                () -> chatService.findByChatId(123L),
                ApiException.class
            );

            assertThat(ex.code()).isEqualTo("CHAT_NOT_FOUND");
            assertThat(ex.messageProp()).isEqualTo("ex.api.chatNotFound");
            assertThat(ex.messageArgs().length).isEqualTo(1);
            assertThat(ex.messageArgs()[0]).isEqualTo("123");
            assertThat(ex.status()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    class RegisterChatTest {

        @Test
        @Transactional
        @Rollback
        void shouldSaveChatWhenChatDoesNotExists() {
            assertFalse(chatRepository.existsByChatId(123L));

            chatService.registerChat(123L);

            assertTrue(chatRepository.existsByChatId(123L));
        }

        @Test
        @Sql(scripts = {"/sql/chats/add-chat.sql"})
        @Transactional
        @Rollback
        void shouldThrowExceptionWhenChatExists() {
            ApiException ex = catchThrowableOfType(
                () -> chatService.registerChat(123L),
                ApiException.class
            );
            assertThat(ex.code()).isEqualTo("CHAT_ALREADY_EXISTS");
            assertThat(ex.messageProp()).isEqualTo("ex.api.chatAlreadyExists");
            assertThat(ex.messageArgs().length).isEqualTo(1);
            assertThat(ex.messageArgs()[0]).isEqualTo("123");
            assertThat(ex.status()).isEqualTo(HttpStatus.CONFLICT);
        }
    }

    @Nested
    class DeleteChatTest {

        @Test
        @Sql(scripts = {"/sql/chats/add-chat.sql"})
        @Transactional
        @Rollback
        void shouldDeleteChatWhenChatExists() {
            assertTrue(chatRepository.existsByChatId(123L));

            chatService.deleteChat(123L);

            assertFalse(chatRepository.existsByChatId(123L));
        }

        @Test
        @Transactional
        void shouldThrowExceptionWhenChatDoesNotExists() {
            ApiException ex = catchThrowableOfType(
                () -> chatService.deleteChat(123L),
                ApiException.class
            );
            assertThat(ex.code()).isEqualTo("CHAT_NOT_FOUND");
            assertThat(ex.messageProp()).isEqualTo("ex.api.chatNotFound");
            assertThat(ex.messageArgs().length).isEqualTo(1);
            assertThat(ex.messageArgs()[0]).isEqualTo("123");
            assertThat(ex.status()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }
}
