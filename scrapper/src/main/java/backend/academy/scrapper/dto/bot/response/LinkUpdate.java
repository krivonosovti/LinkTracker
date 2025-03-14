package backend.academy.scrapper.dto.bot.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class LinkUpdate {
    private Long id;
    private String url;
    private String description;
    private List<Long> tgChatIds;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Long> getTgChatIds() {
        return tgChatIds;
    }

    public void setTgChatIds(List<Long> tgChatIds) {
        this.tgChatIds = tgChatIds;
    }
}
