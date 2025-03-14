package backend.academy.bot.stateMachine;

import java.util.List;

public class Conversation {
    private State state;
    private String link;
    private List<String> tags;
    private List<String> filters;

    public Conversation() {
        this.state = State.START;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
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

    public void reset() {
        this.state = State.COMMAND_WAITING;
        this.link = null;
        this.tags = null;
        this.filters = null;
    }
}
