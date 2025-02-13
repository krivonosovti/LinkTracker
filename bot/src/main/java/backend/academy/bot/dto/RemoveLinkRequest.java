package backend.academy.bot.dto;

public class RemoveLinkRequest {
    private String link;

    public RemoveLinkRequest() {
    }

    public RemoveLinkRequest(String link) {
        this.link = link;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
