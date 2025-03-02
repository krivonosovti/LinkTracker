package backend.academy.scrapper.service.api;


import backend.academy.scrapper.client.GithubClient;
import backend.academy.scrapper.dto.github.CommitDto;
import backend.academy.scrapper.dto.github.IssueDto;
import backend.academy.scrapper.dto.github.RepositoryDto;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@RequiredArgsConstructor
@Service
public class GithubService {

    private final GithubClient githubClient;

    public Optional<String> getRepoCommitsResponse(RepositoryDto repository, OffsetDateTime lastCheckedAt) {
        List<CommitDto> commits = githubClient.getRepoCommits(
            repository.owner(),
            repository.repo(),
            lastCheckedAt
        );
        return getCommitsResponse(commits);
    }

    public Optional<String> getBranchCommitsResponse(
        RepositoryDto repository,
        String branch,
        OffsetDateTime lastCheckedAt
    ) {
        List<CommitDto> commits = githubClient.getBranchCommits(
            repository.owner(),
            repository.repo(),
            branch,
            lastCheckedAt
        );
        return getCommitsResponse(commits);
    }

    public Optional<String> getIssuesAndPullsResponse(RepositoryDto repository, OffsetDateTime lastCheckedAt) {
        List<IssueDto> issuesAndPulls =
            githubClient.getIssuesAndPulls(repository.owner(), repository.repo(), lastCheckedAt);
        return Optional.of(issuesAndPulls)
            .filter(it -> !CollectionUtils.isEmpty(issuesAndPulls))
            .map(this::getIssuesAndPullsResponseMessage);
    }

    public Optional<String> getIssueResponse(RepositoryDto repository, String num, OffsetDateTime lastCheckedAt) {
        IssueDto issue = githubClient.getIssue(repository.owner(), repository.repo(), num);
        return getBaseIssueResponse(issue, lastCheckedAt, true);
    }

    public Optional<String> getPullRequestResponse(RepositoryDto repository, String num, OffsetDateTime lastCheckedAt) {
        IssueDto pullRequest = githubClient.getPullRequest(repository.owner(), repository.repo(), num);
        return getBaseIssueResponse(pullRequest, lastCheckedAt, false);
    }

    private Optional<String> getCommitsResponse(List<CommitDto> commits) {
        return Optional.of(commits)
            .filter(it -> !CollectionUtils.isEmpty(commits))
            .map(this::getCommitsResponseMessage);
    }

    private Optional<String> getBaseIssueResponse(IssueDto issue, OffsetDateTime lastCheckedAt, boolean isIssue) {
        return Optional.of(issue)
            .filter(it -> it.updatedAt().isAfter(lastCheckedAt))
            .map(it -> getBaseIssueResponseMessage(it, isIssue));
    }

    private String getCommitsResponseMessage(List<CommitDto> commits) {
        return "✔ new commits were pushed:\n"
            + commits.stream()
            .map(CommitDto::getResponseBulletPoint)
            .collect(Collectors.joining("\n"));
    }

    private String getIssuesAndPullsResponseMessage(List<IssueDto> issues) {
        return "✔ updates in issues and pull requests:\n"
            + issues.stream()
            .map(IssueDto::getResponseBulletPoint)
            .collect(Collectors.joining("\n"));
    }

    private String getBaseIssueResponseMessage(IssueDto issue, boolean isIssue) {
        return String.format("➜ %s was updated [%s]", isIssue ? "issue" : "PR", issue.title());
    }
}
