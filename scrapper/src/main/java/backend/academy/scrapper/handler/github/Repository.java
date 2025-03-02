package backend.academy.scrapper.handler.github;

import backend.academy.scrapper.dto.github.RepositoryDto;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.handler.LinkUpdateHandler;
import backend.academy.scrapper.service.api.GithubService;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class Repository implements LinkUpdateHandler {

    @Value("${app.link-sources.github.handlers.repository.regex}")
    private String regex;
    private final GithubService githubService;

    @Override
    public String regex() {
        return regex;
    }

    @Override
    public Optional<String> getLinkUpdate(Link link) {
        MatchResult matcher = linkMatcher(link);
        RepositoryDto repository = new RepositoryDto(matcher.group("owner"), matcher.group("repo"));
        Optional<String> commits = githubService.getRepoCommitsResponse(repository, link.checkedAt());
        Optional<String> issues = githubService.getIssuesAndPullsResponse(repository, link.checkedAt());
        return Stream.of(commits, issues)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.collectingAndThen(
                Collectors.joining("\n\n"),
                it -> it.isEmpty() ? Optional.empty() : Optional.of(it)
            ));
    }
}
