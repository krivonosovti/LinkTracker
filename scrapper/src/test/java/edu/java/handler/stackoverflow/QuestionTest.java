package edu.java.handler.stackoverflow;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.regex.MatchResult;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.enums.LinkType;
import backend.academy.scrapper.handler.stackoverflow.Question;
import backend.academy.scrapper.service.StackoverflowService;
import backend.academy.scrapper.util.LinkSourceUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = Question.class)
class QuestionTest {

    @Autowired
    private Question question;
    @MockBean
    private StackoverflowService stackoverflowService;
    static MockedStatic<LinkSourceUtil> linkSourceUtilMock;

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

    @BeforeAll
    public static void init() {
        linkSourceUtilMock = mockStatic(LinkSourceUtil.class);
        linkSourceUtilMock.when(() -> LinkSourceUtil.getDomain(any())).thenReturn("stackoverflow.com");
    }

    @AfterAll
    public static void close() {
        linkSourceUtilMock.close();
    }

    @Nested
    class RegexTest {

        @Test
        void regexTest() {
            String expected = "/(?:questions|q)/(?<id>[\\d]+)[/\\w-\\d]*";

            String actual = question.regex();

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class LinkMatcherTest {

        @Test
        void linkMatcherTest() {
            MatchResult matcher = question.linkMatcher(LINK);

            assertThat(matcher.groupCount()).isEqualTo(1);
            assertThat(matcher.group(1)).isEqualTo("24840667");
        }
    }

    @Nested
    class GetLinkUpdateTest {

        @Test
        void shouldReturnResponseWhenThereAreUpdates() {
            doReturn(Optional.of("question was updated"))
                .when(stackoverflowService)
                .getQuestionResponse(anyString(), any(Link.class));
            doReturn(Optional.of("new answers"))
                .when(stackoverflowService)
                .getQuestionAnswersResponse(anyString(), any(OffsetDateTime.class));

            Optional<String> update = question.getLinkUpdate(LINK);

            verify(stackoverflowService).getQuestionResponse("24840667", LINK);
            verify(stackoverflowService).getQuestionAnswersResponse("24840667", CHECKED_AT);
            assertThat(update).isPresent();
            assertThat(update.get()).isEqualTo("question was updated:\nnew answers");
        }

        @Test
        void shouldReturnEmptyWhenThereAreNoUpdates() {
            doReturn(Optional.empty())
                .when(stackoverflowService)
                .getQuestionResponse(anyString(), any(Link.class));

            Optional<String> update = question.getLinkUpdate(LINK);

            verify(stackoverflowService).getQuestionResponse("24840667", LINK);
            verify(stackoverflowService, never()).getQuestionAnswersResponse(anyString(), any(OffsetDateTime.class));
            assertThat(update).isEmpty();
        }
    }
}
