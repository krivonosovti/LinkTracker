package edu.java.handler.github;

import backend.academy.scrapper.dto.github.RepositoryDto;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.enums.LinkType;
import backend.academy.scrapper.handler.github.Issue;
import backend.academy.scrapper.service.GithubService;
import backend.academy.scrapper.util.LinkSourceUtil;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.regex.MatchResult;
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
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = Issue.class)
class IssueTest {

    @Autowired
    private Issue issue;
    @MockBean
    private GithubService githubService;
    static MockedStatic<LinkSourceUtil> linkSourceUtilMock;

    private static final OffsetDateTime CHECKED_AT = OffsetDateTime.of(
        LocalDate.of(2024, 1, 1),
        LocalTime.of(0, 0, 0),
        ZoneOffset.UTC
    );
    private static final Link LINK = Link.builder()
        .id(1L)
        .linkType(LinkType.GITHUB)
        .url("https://github.com/JetBrains/kotlin/issues/1")
        .checkedAt(CHECKED_AT)
        .build();
    private static final RepositoryDto REPOSITORY = new RepositoryDto("JetBrains", "kotlin");

    @BeforeAll
    public static void init() {
        linkSourceUtilMock = mockStatic(LinkSourceUtil.class);
        linkSourceUtilMock.when(() -> LinkSourceUtil.getDomain(any())).thenReturn("github.com");
    }

    @AfterAll
    public static void close() {
        linkSourceUtilMock.close();
    }

    @Nested
    class RegexTest {

        @Test
        void regexTest() {
            String expected = "/(?<owner>[\\w-\\.]+)/(?<repo>[\\w-\\.]+)/issues/(?<num>\\d+)";

            String actual = issue.regex();

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class LinkMatcherTest {

        @Test
        void linkMatcherTest() {
            MatchResult matcher = issue.linkMatcher(LINK);

            assertThat(matcher.groupCount()).isEqualTo(3);
            assertThat(matcher.group(1)).isEqualTo("JetBrains");
            assertThat(matcher.group(2)).isEqualTo("kotlin");
            assertThat(matcher.group(3)).isEqualTo("1");
        }
    }

    @Nested
    class GetLinkUpdateTest {

        @Test
        void shouldReturnResponseWhenThereAreUpdates() {
            doReturn(Optional.of("new issues"))
                .when(githubService)
                .getIssueResponse(any(RepositoryDto.class), anyString(), any(OffsetDateTime.class));

            Optional<String> update = issue.getLinkUpdate(LINK);

            verify(githubService).getIssueResponse(REPOSITORY, "1", CHECKED_AT);
            assertThat(update).isPresent();
            assertThat(update.get()).isEqualTo("new issues");
        }

        @Test
        void shouldReturnEmptyWhenThereAreNoUpdates() {
            doReturn(Optional.empty())
                .when(githubService)
                .getIssueResponse(any(RepositoryDto.class), anyString(), any(OffsetDateTime.class));

            Optional<String> update = issue.getLinkUpdate(LINK);

            verify(githubService).getIssueResponse(REPOSITORY, "1", CHECKED_AT);
            assertThat(update).isEmpty();
        }
    }
}
