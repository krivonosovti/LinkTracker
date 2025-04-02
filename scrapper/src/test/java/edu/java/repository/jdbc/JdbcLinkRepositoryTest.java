package edu.java.repository.jdbc;


import backend.academy.scrapper.configuration.DatabaseAccessConfig;
import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.enums.LinkStatus;
import backend.academy.scrapper.enums.LinkType;
import backend.academy.scrapper.repository.jdbc.JdbcLinkRepository;
import backend.academy.scrapper.repository.jdbc.mapper.LinkMapper;
import backend.academy.scrapper.repository.jdbc.mapper.LinkWithChatsResultSetExtractor;
import edu.java.integration.IntegrationTest;
import edu.java.integration.config.JdbcTestConfig;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {JdbcLinkRepository.class, LinkMapper.class, LinkWithChatsResultSetExtractor.class})
@ContextConfiguration(classes = {JdbcTestConfig.class, DatabaseAccessConfig.JdbcAccessConfig.class})
@TestPropertySource(properties = {"app.database-access-type=jdbc"})
class JdbcLinkRepositoryTest extends IntegrationTest {

    @Autowired
    private JdbcLinkRepository linkRepository;

    private static final OffsetDateTime CHECKED_AT = OffsetDateTime.of(
        LocalDate.of(2024, 1, 1),
        LocalTime.of(0, 0, 0),
        ZoneOffset.UTC
    );
    private static final Link LINK =
        Link.builder()
            .linkType(LinkType.GITHUB)
            .url("https://github.com/JetBrains/kotlin")
            .checkedAt(CHECKED_AT)
            .build();
    private static final Chat CHAT = Chat.builder().id(1L).chatId(123L).build();

    @Nested
    class SaveLinkTest {

        @Test
        @Transactional
        @Rollback
        void shouldReturnSavedLink() {
            Link saved = linkRepository.save(LINK);

            assertThat(saved).isNotNull();
            assertThat(saved.id()).isNotNull();
            assertThat(saved.status()).isEqualTo(LinkStatus.ACTIVE);
        }
    }

    @Nested
    class FindAllByChatTest {

        @Test
        @Sql(scripts = {"/sql/chats/add-chat.sql", "/sql/links/add-links-list.sql",
            "/sql/chats-links/add-links-list-to-chat.sql"})
        @Transactional
        @Rollback
        void shouldReturnLinks() {
            List<Link> links = linkRepository.findAllByChat(CHAT);

            assertThat(links.size()).isEqualTo(2);

            Link first = links.get(0);
            assertThat(first.id()).isEqualTo(1);
            assertThat(first.linkType()).isEqualTo(LinkType.GITHUB);
            assertThat(first.url()).isEqualTo("https://github.com/JetBrains/kotlin");
            assertThat(first.checkedAt()).isEqualTo(CHECKED_AT);
            assertThat(first.status()).isEqualTo(LinkStatus.ACTIVE);

            Link second = links.get(1);
            assertThat(second.id()).isEqualTo(2);
            assertThat(second.linkType()).isEqualTo(LinkType.STACKOVERFLOW);
            assertThat(second.url()).isEqualTo("https://stackoverflow.com/questions/24840667");
            assertThat(second.checkedAt()).isEqualTo(CHECKED_AT);
            assertThat(second.status()).isEqualTo(LinkStatus.ACTIVE);
        }
    }

    @Nested
    class FindByUrlTest {

        @Test
        @Sql(scripts = {"/sql/links/add-link.sql"})
        @Transactional
        @Rollback
        void shouldReturnLink() {
            Optional<Link> link = linkRepository.findByUrl("https://github.com/JetBrains/kotlin");

            assertThat(link).isPresent();
            assertThat(link.get().id()).isEqualTo(1);
            assertThat(link.get().linkType()).isEqualTo(LinkType.GITHUB);
        }

        @Test
        @Transactional
        void shouldReturnNullWhenLinkDoesNotExists() {
            Optional<Link> link = linkRepository.findByUrl("https://github.com/JetBrains/kotlin");

            assertThat(link).isEmpty();
        }
    }

    @Nested
    class UpdateStatusTest {

        @Test
        @Sql(scripts = {"/sql/links/add-link.sql"})
        @Transactional
        @Rollback
        void shouldReturnTrueWhenStatusWasUpdated() {
            boolean isUpdated = linkRepository.updateStatus(Link.builder().id(1L).build(), LinkStatus.BROKEN);

            assertTrue(isUpdated);

            Optional<Link> link = linkRepository.findByUrl(LINK.url());

            assertThat(link).isPresent();
            assertThat(link.get().id()).isEqualTo(1);
            assertThat(link.get().status()).isEqualTo(LinkStatus.BROKEN);
        }
    }

    @Nested
    class UpdateCheckedAtTest {

        @Test
        @Sql(scripts = {"/sql/links/add-link.sql"})
        @Transactional
        @Rollback
        void shouldReturnTrueWhenCheckedAtWasUpdated() {
            boolean isUpdated = linkRepository.updateCheckedAt(Link.builder().id(1L).build(), CHECKED_AT.plusHours(1));

            assertTrue(isUpdated);

            Optional<Link> link = linkRepository.findByUrl(LINK.url());

            assertThat(link).isPresent();
            assertThat(link.get().id()).isEqualTo(1);
            assertThat(link.get().checkedAt()).isEqualTo(CHECKED_AT.plusHours(1));
        }
    }

    @Nested
    class FindAllWithStatusAndOlderThanTest {

        @Test
        @Sql(scripts = {"/sql/chats/add-chat.sql", "/sql/links/add-links-list.sql", "/sql/links/add-old-link.sql",
            "/sql/chats-links/add-links-list-to-chat.sql", "/sql/chats-links/add-old-link-to-chat.sql"})
        @Transactional
        @Rollback
        void shouldReturnLinksWhenOldLinksExist() {
            List<Link> links =
                linkRepository.findAllWithStatusAndOlderThan(LinkStatus.ACTIVE, CHECKED_AT, 50);

            assertThat(links.size()).isEqualTo(1);

            Link link = links.get(0);
            assertThat(link.id()).isEqualTo(3);
            assertThat(link.linkType()).isEqualTo(LinkType.GITHUB);
            assertThat(link.checkedAt()).isEqualTo(CHECKED_AT.minusHours(6));
            assertThat(link.status()).isEqualTo(LinkStatus.ACTIVE);
            assertThat(link.chats().size()).isEqualTo(1);
            assertThat(link.chats())
                .usingElementComparator(Comparator
                    .comparing(Chat::getId)
                    .thenComparing(Chat::getChatId))
                .containsExactlyInAnyOrderElementsOf(Set.of(CHAT));
        }

        @Test
        @Sql(scripts = {"/sql/chats/add-chat.sql", "/sql/links/add-links-list.sql",
            "/sql/chats-links/add-links-list-to-chat.sql"})
        @Transactional
        @Rollback
        void shouldReturnEmptyListWhenOldLinksDoNotExist() {
            List<Link> links =
                linkRepository.findAllWithStatusAndOlderThan(LinkStatus.ACTIVE, CHECKED_AT, 50);

            assertThat(links.size()).isEqualTo(0);
        }
    }
}
