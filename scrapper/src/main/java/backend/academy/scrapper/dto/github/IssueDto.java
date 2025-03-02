package backend.academy.scrapper.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record IssueDto(@JsonProperty("html_url") String htmlUrl, String title,
                       @JsonProperty("updated_at") OffsetDateTime updatedAt) {

    public String getResponseBulletPoint() {
        return String.format("➜ %s [%s]", title, htmlUrl);
    }
}
