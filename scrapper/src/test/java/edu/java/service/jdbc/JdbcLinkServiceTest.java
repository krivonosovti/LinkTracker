package edu.java.service.jdbc;

import backend.academy.scrapper.dto.bot.response.LinkResponse;
import backend.academy.scrapper.dto.bot.response.ListLinksResponse;
import backend.academy.scrapper.entity.Chat;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.enums.LinkStatus;
import backend.academy.scrapper.enums.LinkType;
import backend.academy.scrapper.exception.ApiException;
import backend.academy.scrapper.repository.ChatLinkRepository;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.service.jdbc.JdbcChatService;
import backend.academy.scrapper.service.jdbc.JdbcLinkService;
import backend.academy.scrapper.util.LinkParser;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JdbcLinkServiceTest {

    @InjectMocks
    private JdbcLinkService linkService;

    @Mock
    private LinkRepository linkRepository;
    @Mock
    private ChatLinkRepository chatLinkRepository;
    @Mock
    private JdbcChatService chatService;
    static MockedStatic<LinkParser> linkParserMock;

    private static final OffsetDateTime CHECKED_AT = OffsetDateTime.of(
        LocalDate.of(2024, 1, 1),
        LocalTime.of(0, 0, 0),
        ZoneOffset.UTC
    );
    private static final Link LINK =
        Link.builder()
            .id(1L)
            .linkType(LinkType.GITHUB)
            .url("https://github.com/JetBrains/kotlin")
            .checkedAt(CHECKED_AT)
            .build();
    private static final Chat CHAT = Chat.builder().id(1L).chatId(123L).build();

    @BeforeAll
    public static void init() {
        linkParserMock = mockStatic(LinkParser.class);
        linkParserMock.when(() -> LinkParser.parseLink(any(String.class)))
            .thenReturn(Link.builder()
                .linkType(LinkType.GITHUB)
                .url("https://github.com/JetBrains/kotlin")
                .build());
    }

    @AfterAll
    public static void close() {
        linkParserMock.close();
    }

    @Nested
    class GetLinksToUpdateTest {

        @Test
        void getLinksToUpdateTest() {
            ArgumentCaptor<LinkStatus> statusCaptor = ArgumentCaptor.forClass(LinkStatus.class);

            linkService.getLinksToUpdate(60, 50);

            verify(linkRepository).findAllWithStatusAndOlderThan(
                statusCaptor.capture(),
                any(OffsetDateTime.class),
                anyInt()
            );
            assertThat(statusCaptor.getValue()).isEqualTo(LinkStatus.ACTIVE);
        }
    }

    @Nested
    class AddLinkToChatTest {

        @Test
        void addAnExistingLinkInDbTest() {
            doReturn(CHAT).when(chatService).findByChatId(anyLong());
            doReturn(Optional.of(LINK)).when(linkRepository).findByUrl(anyString());
            doReturn(false).when(chatLinkRepository).isLinkAddedToChat(any(Chat.class), any(Link.class));

            LinkResponse response =
                linkService.addLinkToChat(123L, "https://github.com/JetBrains/kotlin");

            verify(linkRepository, never()).save(any(Link.class));
            verify(chatLinkRepository).addLinkToChat(any(Chat.class), any(Link.class));

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getUrl()).isEqualTo("https://github.com/JetBrains/kotlin");
        }

        @Test
        void addNewLinkTest() {
            doReturn(CHAT).when(chatService).findByChatId(anyLong());
            doReturn(Optional.empty()).when(linkRepository).findByUrl(anyString());
            doReturn(LINK).when(linkRepository).save(any(Link.class));

            LinkResponse response =
                linkService.addLinkToChat(123L, "https://github.com/JetBrains/kotlin");

            verify(chatLinkRepository, never()).isLinkAddedToChat(any(Chat.class), any(Link.class));
            verify(linkRepository).save(any(Link.class));
            verify(chatLinkRepository).addLinkToChat(any(Chat.class), any(Link.class));

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getUrl()).isEqualTo("https://github.com/JetBrains/kotlin");
        }

        @Test
        void shouldThrowExceptionWhenLinkExists() {
            doReturn(CHAT).when(chatService).findByChatId(anyLong());
            doReturn(Optional.of(LINK)).when(linkRepository).findByUrl(anyString());
            doReturn(true).when(chatLinkRepository).isLinkAddedToChat(any(Chat.class), any(Link.class));

            ApiException ex = catchThrowableOfType(
                () -> linkService.addLinkToChat(123L, "https://github.com/JetBrains/kotlin"),
                ApiException.class
            );
            assertThat(ex.code()).isEqualTo("LINK_ALREADY_EXISTS");
            assertThat(ex.messageProp()).isEqualTo("ex.api.linkAlreadyExists");
            assertThat(ex.messageArgs().length).isEqualTo(1);
            assertThat(ex.messageArgs()[0]).isEqualTo("https://github.com/JetBrains/kotlin");
            assertThat(ex.status()).isEqualTo(HttpStatus.CONFLICT);
        }
    }

    @Nested
    class RemoveLinkFromChatTest {

        @Test
        void removeLinkTest() {
            doReturn(CHAT).when(chatService).findByChatId(anyLong());
            doReturn(Optional.of(LINK)).when(linkRepository).findByUrl(anyString());
            doReturn(true).when(chatLinkRepository).isLinkAddedToChat(any(Chat.class), any(Link.class));

            LinkResponse response =
                linkService.removeLinkFromChat(123L, "https://github.com/JetBrains/kotlin");

            verify(chatLinkRepository).removeLinkFromChat(any(Chat.class), any(Link.class));

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getUrl()).isEqualTo("https://github.com/JetBrains/kotlin");
        }

        @Test
        void shouldThrowExceptionWhenLinkDoesNotExists() {
            doReturn(CHAT).when(chatService).findByChatId(anyLong());
            doReturn(Optional.empty()).when(linkRepository).findByUrl(anyString());

            ApiException ex = catchThrowableOfType(
                () -> linkService.removeLinkFromChat(123L, "https://github.com/JetBrains/kotlin"),
                ApiException.class
            );
            assertThat(ex.code()).isEqualTo("LINK_NOT_FOUND");
            assertThat(ex.messageProp()).isEqualTo("ex.api.linkNotFound");
            assertThat(ex.messageArgs().length).isEqualTo(1);
            assertThat(ex.messageArgs()[0]).isEqualTo("https://github.com/JetBrains/kotlin");
            assertThat(ex.status()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void shouldThrowExceptionWhenLinkNotAdded() {
            doReturn(CHAT).when(chatService).findByChatId(anyLong());
            doReturn(Optional.of(LINK)).when(linkRepository).findByUrl(anyString());
            doReturn(false).when(chatLinkRepository).isLinkAddedToChat(any(Chat.class), any(Link.class));

            ApiException ex = catchThrowableOfType(
                () -> linkService.removeLinkFromChat(123L, "https://github.com/JetBrains/kotlin"),
                ApiException.class
            );
            assertThat(ex.code()).isEqualTo("LINK_NOT_FOUND");
            assertThat(ex.messageProp()).isEqualTo("ex.api.linkNotFound");
            assertThat(ex.messageArgs().length).isEqualTo(1);
            assertThat(ex.messageArgs()[0]).isEqualTo("https://github.com/JetBrains/kotlin");
            assertThat(ex.status()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    class GetChatLinksTest {

        @Test
        void getChatLinksTest() {
            doReturn(CHAT).when(chatService).findByChatId(anyLong());
            doReturn(List.of(LINK)).when(linkRepository).findAllByChat(any(Chat.class));

            ListLinksResponse response = linkService.getChatLinks(123L);

            assertThat(response.getLinks().size()).isEqualTo(1);
            assertThat(response.getSize()).isEqualTo(1);

            LinkResponse link = response.getLinks().get(0);
            assertThat(link.getId()).isEqualTo(1L);
            assertThat(link.getUrl()).isEqualTo("https://github.com/JetBrains/kotlin");
        }
    }
}
