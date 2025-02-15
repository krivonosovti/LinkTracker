package backend.academy.scrapper.dto;

import java.util.List;
import java.util.Objects;

public class LinkResponse {
    private Long id;
    private String url;
    private List<String> tags;
    private List<String> filters;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LinkResponse response = (LinkResponse) o;
        return Objects.equals(id, response.id) && Objects.equals(url, response.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, url);
    }

    public LinkResponse(Long id, String url) {
        this.id = id;
        this.url = url;
        this.tags = null;
        this.filters = null;
    }

    public LinkResponse(Long id, AddLinkRequest request) {
        this.id = id;
        this.url = request.getLink();
        this.tags = request.getTags();
        this.filters = request.getFilters();
    }

    public LinkResponse(Long id, String url, List<String> tags, List<String> filters) {
        this.id = id;
        this.url = url;
        this.tags = tags;
        this.filters = filters;
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
