package edu.java.service.sender;

import java.net.URI;
import java.util.List;
import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.dto.bot.response.LinkUpdate;
import backend.academy.scrapper.service.sender.HttpUpdateSender;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HttpUpdateSenderTest {

    @InjectMocks
    private HttpUpdateSender updateSender;
    @Mock
    private BotClient botClient;
    @Captor
    private ArgumentCaptor<LinkUpdate> responseCaptor;

    private static final LinkUpdate UPDATE =
        new LinkUpdate(1L, "https://github.com/JetBrains/kotlin", "new update", List.of(2L));

    @Nested
    class SendLinkUpdateTest {

        @Test
        void successfulSendUpdate() {
            boolean isSent = updateSender.send(UPDATE);

            assertThat(isSent).isTrue();
            verify(botClient).postUpdate(responseCaptor.capture());
            LinkUpdate response = responseCaptor.getValue();
            assertThat(response.getUrl().toString()).isEqualTo("https://github.com/JetBrains/kotlin");
            assertThat(response.getDescription()).isEqualTo("new update");
            assertThat(response.getTgChatIds().size()).isEqualTo(1);
            assertThat(response.getTgChatIds().get(0)).isEqualTo(2L);
        }

        @Test
        void errorSendUpdate() {
            doThrow(new WebClientResponseException(
                HttpStatus.BAD_REQUEST,
                "",
                null,
                "bad request".getBytes(),
                null,
                null
            )).when(botClient).postUpdate(any());
            boolean isSent = updateSender.send(UPDATE);

            assertThat(isSent).isFalse();
            verify(botClient).postUpdate(responseCaptor.capture());
            LinkUpdate response = responseCaptor.getValue();
            assertThat(response.getUrl().toString()).isEqualTo("https://github.com/JetBrains/kotlin");
            assertThat(response.getDescription()).isEqualTo("new update");
            assertThat(response.getTgChatIds().size()).isEqualTo(1);
            assertThat(response.getTgChatIds().get(0)).isEqualTo(2L);
        }
    }
}
