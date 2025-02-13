package backend.academy.bot.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

public class TelegramChat {
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
