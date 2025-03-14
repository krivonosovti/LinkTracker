package backend.academy.scrapper.dto.bot.response;

import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class LinkResponse {
    private Long id;
    private String url;
    private List<String> tags;
    private List<String> filters;

    public LinkResponse(Long id, String url) {
        this.id = id;
        this.url = url;
        this.tags = Collections.emptyList();
        this.filters = Collections.emptyList();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getFilters() {
        return filters;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }
}

