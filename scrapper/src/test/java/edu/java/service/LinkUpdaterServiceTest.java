package edu.java.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import backend.academy.scrapper.configuration.ScrapperConfig;
import backend.academy.scrapper.dto.bot.response.LinkUpdate;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.enums.LinkStatus;
import backend.academy.scrapper.enums.LinkType;
import backend.academy.scrapper.handler.github.Repository;
import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.service.LinkUpdaterService;
import backend.academy.scrapper.service.sender.HttpUpdateSender;
import backend.academy.scrapper.util.LinkSourceUtil;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = {LinkUpdaterService.class, LinkSourceUtil.class})
@EnableConfigurationProperties(value = ScrapperConfig.class)
class LinkUpdaterServiceTest {

    @Autowired
    private LinkUpdaterService linkUpdaterService;
    @MockBean
    private LinkService linkService;
    @MockBean
    private HttpUpdateSender updateSender;
    @MockBean
    private Repository repository;
    @Captor
    private ArgumentCaptor<LinkUpdate> updateCaptor;

    private static final OffsetDateTime CHECKED_AT = OffsetDateTime.of(
        LocalDate.of(2024, 1, 1),
        LocalTime.of(0, 0, 0),
        ZoneOffset.UTC
    );
    private static final Link LINK = Link.builder()
        .id(1L)
        .linkType(LinkType.GITHUB)
        .url("https://github.com/JetBrains/kotlin")
        .checkedAt(CHECKED_AT)
        .build();

    @Nested
    class UpdateLinksTest {

        @Test
        void shouldNotProcessUpdateWhenLinkSourceIsNotConfigured() {
            Link link = Link.builder()
                .id(1L)
                .linkType(null)
                .url("")
                .checkedAt(CHECKED_AT)
                .build();
            doReturn(List.of(link)).when(linkService).getLinksToUpdate(any(), any());

            linkUpdaterService.updateLinks();

            verify(repository, never()).getLinkUpdate(any(Link.class));
            verify(updateSender, never()).send(any(LinkUpdate.class));
            verify(linkService, never()).updateCheckedAt(any(Link.class), any(OffsetDateTime.class));
        }

        @Test
        void shouldNotProcessUpdateWhenNoUpdateHandlerIsMatch() {
            Link link = Link.builder()
                .id(1L)
                .linkType(LinkType.GITHUB)
                .url("https://github.com/JetBrains/kotlin/stargazers")
                .checkedAt(CHECKED_AT)
                .build();
            doReturn(List.of(link)).when(linkService).getLinksToUpdate(any(), any());

            linkUpdaterService.updateLinks();

            verify(repository, never()).getLinkUpdate(any(Link.class));
            verify(updateSender, never()).send(any(LinkUpdate.class));
            verify(linkService, never()).updateCheckedAt(any(Link.class), any(OffsetDateTime.class));
        }

        @Test
        void shouldUpdateCheckAtWhenThereAreNoUpdates() {
            doReturn(List.of(LINK)).when(linkService).getLinksToUpdate(any(), any());
            doReturn(Optional.empty()).when(repository).getLinkUpdate(any(Link.class));

            linkUpdaterService.updateLinks();

            verify(repository).getLinkUpdate(any(Link.class));
            verify(updateSender, never()).send(any(LinkUpdate.class));
            verify(linkService).updateCheckedAt(any(Link.class), any(OffsetDateTime.class));
        }

        @Test
        void shouldSendUpdateAndUpdateCheckAtWhenThereAreUpdates() {
            doReturn(List.of(LINK)).when(linkService).getLinksToUpdate(any(), any());
            doReturn(Optional.of("new update")).when(repository).getLinkUpdate(any(Link.class));
            doReturn(true).when(updateSender).send(any(LinkUpdate.class));

            linkUpdaterService.updateLinks();

            verify(repository).getLinkUpdate(any(Link.class));
            verify(updateSender).send(updateCaptor.capture());
            verify(linkService).updateCheckedAt(any(Link.class), any(OffsetDateTime.class));
            assertThat(updateCaptor.getValue().getDescription()).isEqualTo("new update");
        }

        @Test
        void shouldNotUpdateCheckAtWhenSendUpdateReturnFalse() {
            doReturn(List.of(LINK)).when(linkService).getLinksToUpdate(any(), any());
            doReturn(Optional.of("new update")).when(repository).getLinkUpdate(any(Link.class));
            doReturn(false).when(updateSender).send(any(LinkUpdate.class));

            linkUpdaterService.updateLinks();

            verify(repository).getLinkUpdate(any(Link.class));
            verify(updateSender).send(updateCaptor.capture());
            verify(linkService, never()).updateCheckedAt(any(Link.class), any(OffsetDateTime.class));
            assertThat(updateCaptor.getValue().getDescription()).isEqualTo("new update");
        }

        @Test
        void shouldUpdateLinkStatusWhenHandlerThrowException() {
            doReturn(List.of(LINK)).when(linkService).getLinksToUpdate(any(), any());
            doThrow(new WebClientResponseException(HttpStatus.BAD_REQUEST, "", null, null, null, null))
                .when(repository).getLinkUpdate(any(Link.class));

            linkUpdaterService.updateLinks();

            verify(repository).getLinkUpdate(any(Link.class));
            verify(updateSender, never()).send(any(LinkUpdate.class));
            verify(linkService, never()).updateCheckedAt(any(Link.class), any(OffsetDateTime.class));
            verify(linkService).updateLinkStatus(LINK, LinkStatus.BROKEN);
        }
    }
}
