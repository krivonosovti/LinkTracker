package backend.academy.bot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TelegramUpdate {
    @JsonProperty("update_id")
    private long updateId;

    @JsonProperty("message")
    private TelegramMessage message;

    public long getUpdateId() {
        return updateId;
    }

    public void setUpdateId(long updateId) {
        this.updateId = updateId;
    }

    public TelegramMessage getMessage() {
        return message;
    }

    public void setMessage(TelegramMessage message) {
        this.message = message;
    }
}
