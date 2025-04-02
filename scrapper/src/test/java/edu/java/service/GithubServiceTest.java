package edu.java.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import backend.academy.scrapper.client.GithubClient;
import backend.academy.scrapper.dto.github.CommitDto;
import backend.academy.scrapper.dto.github.IssueDto;
import backend.academy.scrapper.dto.github.RepositoryDto;
import backend.academy.scrapper.service.GithubService;
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

@ExtendWith(MockitoExtension.class)
class GithubServiceTest {

    @InjectMocks
    private GithubService githubService;
    @Mock
    private GithubClient githubClient;

    private static final OffsetDateTime CHECKED_AT = OffsetDateTime.of(
        LocalDate.of(2024, 1, 1),
        LocalTime.of(0, 0, 0),
        ZoneOffset.UTC
    );
    private static final RepositoryDto REPOSITORY = new RepositoryDto("JetBrains", "kotlin");

    @Nested
    class RepoCommitsResponseTest {

        @Test
        void shouldReturnResponseWhenThereAreUpdates() {
            String expectedResponse = "✔ new commits were pushed:\n"
                + "➜ commit 1 [link 1]\n"
                + "➜ commit 2 [link 2]";
            List<CommitDto> commits = List.of(
                new CommitDto(new CommitDto.Commit("commit 1"), "link 1"),
                new CommitDto(new CommitDto.Commit("commit 2"), "link 2")
            );
            doReturn(commits).when(githubClient).getRepoCommits("JetBrains", "kotlin", CHECKED_AT);

            Optional<String> actualResponse = githubService.getRepoCommitsResponse(REPOSITORY, CHECKED_AT);

            assertThat(actualResponse).isPresent();
            assertThat(actualResponse.get()).isEqualTo(expectedResponse);
        }

        @Test
        void shouldReturnEmptyResponseWhenThereAreNoUpdates() {
            List<CommitDto> commits = List.of();
            doReturn(commits).when(githubClient).getRepoCommits("JetBrains", "kotlin", CHECKED_AT);

            Optional<String> actualResponse = githubService.getRepoCommitsResponse(REPOSITORY, CHECKED_AT);

            assertThat(actualResponse).isEmpty();
        }

        @Test
        void shouldThrowExceptionWhenClientTrowException() {
            doThrow(new RuntimeException("client error")).when(githubClient)
                .getRepoCommits("JetBrains", "kotlin", CHECKED_AT);

            assertThatThrownBy(() -> githubService.getRepoCommitsResponse(REPOSITORY, CHECKED_AT))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("client error");
        }
    }

    @Nested
    class RepositoryBranchCommitsResponseTest {

        @Test
        void shouldReturnResponseWhenThereAreUpdates() {
            String expectedResponse = "✔ new commits were pushed:\n"
                + "➜ commit 1 [link 1]\n"
                + "➜ commit 2 [link 2]";

            List<CommitDto> commits = List.of(
                new CommitDto(new CommitDto.Commit("commit 1"), "link 1"),
                new CommitDto(new CommitDto.Commit("commit 2"), "link 2")
            );
            doReturn(commits).when(githubClient)
                .getBranchCommits("JetBrains", "kotlin", "branch-name", CHECKED_AT);

            Optional<String> actualResponse =
                githubService.getBranchCommitsResponse(REPOSITORY, "branch-name", CHECKED_AT);

            assertThat(actualResponse).isPresent();
            assertThat(actualResponse.get()).isEqualTo(expectedResponse);
        }

        @Test
        void shouldReturnEmptyResponseWhenThereAreNoUpdates() {
            List<CommitDto> commits = List.of();
            doReturn(commits).when(githubClient)
                .getBranchCommits("JetBrains", "kotlin", "branch-name", CHECKED_AT);

            Optional<String> actualResponse =
                githubService.getBranchCommitsResponse(REPOSITORY, "branch-name", CHECKED_AT);

            assertThat(actualResponse).isEmpty();
        }

        @Test
        void shouldThrowExceptionWhenClientTrowException() {
            doThrow(new RuntimeException("client error")).when(githubClient)
                .getBranchCommits("JetBrains", "kotlin", "branch-name", CHECKED_AT);

            assertThatThrownBy(() -> githubService.getBranchCommitsResponse(REPOSITORY, "branch-name", CHECKED_AT))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("client error");
        }
    }

    @Nested
    class IssuesAndPullsResponseTest {

        @Test
        void shouldReturnResponseWhenThereAreUpdates() {
            String expectedResponse = "✔ updates in issues and pull requests:\n"
                + "➜ issue 1 [link 1]\n"
                + "➜ issue 2 [link 2]";

            List<IssueDto> issues = List.of(
                new IssueDto("link 1", "issue 1", null),
                new IssueDto("link 2", "issue 2", null)
            );
            doReturn(issues).when(githubClient).getIssuesAndPulls("JetBrains", "kotlin", CHECKED_AT);

            Optional<String> actualResponse =
                githubService.getIssuesAndPullsResponse(REPOSITORY, CHECKED_AT);

            assertThat(actualResponse).isPresent();
            assertThat(actualResponse.get()).isEqualTo(expectedResponse);
        }

        @Test
        void shouldReturnEmptyResponseWhenThereAreNoUpdates() {
            List<IssueDto> issues = List.of();
            doReturn(issues).when(githubClient).getIssuesAndPulls("JetBrains", "kotlin", CHECKED_AT);

            Optional<String> actualResponse =
                githubService.getIssuesAndPullsResponse(REPOSITORY, CHECKED_AT);

            assertThat(actualResponse).isEmpty();
        }

        @Test
        void shouldThrowExceptionWhenClientTrowException() {
            doThrow(new RuntimeException("client error")).when(githubClient)
                .getIssuesAndPulls("JetBrains", "kotlin", CHECKED_AT);

            assertThatThrownBy(() -> githubService.getIssuesAndPullsResponse(REPOSITORY, CHECKED_AT))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("client error");
        }
    }

    @Nested
    class IssueResponseTest {

        @Test
        void shouldReturnResponseWhenThereAreUpdates() {
            String expectedResponse = "➜ issue was updated [title]";

            IssueDto issue = new IssueDto("link", "title", CHECKED_AT.plusDays(1));
            doReturn(issue).when(githubClient).getIssue("JetBrains", "kotlin", "1");

            Optional<String> actualResponse = githubService.getIssueResponse(REPOSITORY, "1", CHECKED_AT);

            assertThat(actualResponse).isPresent();
            assertThat(actualResponse.get()).isEqualTo(expectedResponse);
        }

        @Test
        void shouldReturnEmptyResponseWhenThereAreNoUpdates() {
            IssueDto issue = new IssueDto("link", "title", CHECKED_AT.minusDays(1));
            doReturn(issue).when(githubClient).getIssue("JetBrains", "kotlin", "1");

            Optional<String> actualResponse = githubService.getIssueResponse(REPOSITORY, "1", CHECKED_AT);

            assertThat(actualResponse).isEmpty();
        }

        @Test
        void shouldThrowExceptionWhenClientTrowException() {
            doThrow(new RuntimeException("client error")).when(githubClient).getIssue("JetBrains", "kotlin", "1");

            assertThatThrownBy(() -> githubService.getIssueResponse(REPOSITORY, "1", CHECKED_AT))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("client error");
        }
    }

    @Nested
    class PullRequestResponseTest {

        @Test
        void shouldReturnResponseWhenThereAreUpdates() {
            String expectedResponse = "➜ PR was updated [title]";
            IssueDto pr = new IssueDto("link", "title", CHECKED_AT.plusDays(1));
            doReturn(pr).when(githubClient).getPullRequest("JetBrains", "kotlin", "1");

            Optional<String> actualResponse =
                githubService.getPullRequestResponse(REPOSITORY, "1", CHECKED_AT);

            assertThat(actualResponse).isPresent();
            assertThat(actualResponse.get()).isEqualTo(expectedResponse);
        }

        @Test
        void shouldReturnEmptyResponseWhenThereAreNoUpdates() {
            IssueDto issue = new IssueDto("link", "title", CHECKED_AT.minusDays(1));
            doReturn(issue).when(githubClient).getPullRequest("JetBrains", "kotlin", "1");

            Optional<String> actualResponse =
                githubService.getPullRequestResponse(REPOSITORY, "1", CHECKED_AT);

            assertThat(actualResponse).isEmpty();
        }

        @Test
        void shouldThrowExceptionWhenClientTrowException() {
            doThrow(new RuntimeException("client error")).when(githubClient).getPullRequest("JetBrains", "kotlin", "1");

            assertThatThrownBy(() -> githubService.getPullRequestResponse(REPOSITORY, "1", CHECKED_AT))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("client error");
        }
    }
}
