package edu.java.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import backend.academy.scrapper.client.StackoverflowClient;
import backend.academy.scrapper.dto.stackoverflow.QuestionAnswerDto;
import backend.academy.scrapper.dto.stackoverflow.QuestionDto;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.enums.LinkStatus;
import backend.academy.scrapper.enums.LinkType;
import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.service.StackoverflowService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StackoverflowServiceTest {

    @InjectMocks
    private StackoverflowService stackoverflowService;
    @Mock
    private StackoverflowClient stackoverflowClient;
    @Mock
    private LinkService linkService;

    private static final OffsetDateTime CHECKED_AT = OffsetDateTime.of(
        LocalDate.of(2024, 1, 1),
        LocalTime.of(0, 0, 0),
        ZoneOffset.UTC
    );
    private static final Link LINK = Link.builder()
        .id(1L)
        .linkType(LinkType.STACKOVERFLOW)
        .url("https://stackoverflow.com/questions/24840667")
        .checkedAt(CHECKED_AT)
        .build();

    @Nested
    class QuestionResponseTest {

        @Test
        void shouldReturnResponseWhenThereAreUpdates() {
            String expectedResponse = "✔ question [title] was updated";

            QuestionDto question = new QuestionDto(List.of(new QuestionDto.Question(CHECKED_AT.plusDays(1), "title")));
            doReturn(question).when(stackoverflowClient).getQuestion("24840667");

            Optional<String> actualResponse = stackoverflowService.getQuestionResponse("24840667", LINK);

            assertThat(actualResponse).isPresent();
            assertThat(actualResponse.get()).isEqualTo(expectedResponse);
        }

        @Test
        void shouldReturnEmptyResponseWhenThereAreNoUpdates() {
            QuestionDto question = new QuestionDto(List.of(new QuestionDto.Question(CHECKED_AT.minusDays(1), "title")));
            doReturn(question).when(stackoverflowClient).getQuestion("24840667");

            Optional<String> actualResponse = stackoverflowService.getQuestionResponse("24840667", LINK);

            assertThat(actualResponse).isEmpty();
        }

        @Test
        void shouldReturnEmptyResponseWhenClientResponseIsEmpty() {
            QuestionDto question = new QuestionDto(List.of());
            doReturn(question).when(stackoverflowClient).getQuestion("24840667");

            Optional<String> actualResponse = stackoverflowService.getQuestionResponse("24840667", LINK);

            verify(linkService).updateLinkStatus(LINK, LinkStatus.BROKEN);
            assertThat(actualResponse).isEmpty();
        }

        @Test
        void shouldThrowExceptionWhenClientTrowException() {
            doThrow(new RuntimeException("client error")).when(stackoverflowClient).getQuestion("24840667");

            assertThatThrownBy(() -> stackoverflowService.getQuestionResponse("24840667", LINK))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("client error");
        }
    }

    @Nested
    class QuestionAnswersResponseTest {

        @Test
        void shouldReturnResponseWhenThereAreUpdates() {
            String expectedResponse = "➜ https://stackoverflow.com/a/1";

            QuestionAnswerDto answer = new QuestionAnswerDto(List.of(
                new QuestionAnswerDto.Answer(CHECKED_AT.plusDays(1), "1", "24840667"),
                new QuestionAnswerDto.Answer(CHECKED_AT.minusDays(1), "2", "24840667")
            ));
            doReturn(answer).when(stackoverflowClient).getQuestionAnswers("24840667");

            Optional<String> actualResponse = stackoverflowService.getQuestionAnswersResponse("24840667", CHECKED_AT);

            assertThat(actualResponse).isPresent();
            assertThat(actualResponse.get()).isEqualTo(expectedResponse);
        }

        @Test
        void shouldReturnEmptyResponseWhenThereAreNoUpdates() {
            doReturn(new QuestionAnswerDto(List.of())).when(stackoverflowClient).getQuestionAnswers("24840667");

            Optional<String> actualResponse = stackoverflowService.getQuestionAnswersResponse("24840667", CHECKED_AT);

            assertThat(actualResponse).isEmpty();
        }

        @Test
        void shouldThrowExceptionWhenClientTrowException() {
            doThrow(new RuntimeException("client error")).when(stackoverflowClient).getQuestionAnswers("24840667");

            assertThatThrownBy(() -> stackoverflowService.getQuestionAnswersResponse("24840667", CHECKED_AT))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("client error");
        }
    }
}
