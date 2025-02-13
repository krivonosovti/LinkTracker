package backend.academy.bot.dto;

import java.util.List;

public class TelegramResponse {
    private boolean ok;
    private List<TelegramUpdate> result;

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public List<TelegramUpdate> getResult() {
        return result;
    }

    public void setResult(List<TelegramUpdate> result) {
        this.result = result;
    }
}

