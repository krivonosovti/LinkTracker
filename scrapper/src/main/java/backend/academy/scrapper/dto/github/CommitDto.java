package backend.academy.scrapper.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CommitDto(Commit commit, @JsonProperty("html_url") String htmlUrl) {

    public String getResponseBulletPoint() {
        return String.format("➜ %s [%s]", commit.message, htmlUrl);
    }

    public record Commit(String message) {
    }
}
