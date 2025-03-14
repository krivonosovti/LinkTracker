package backend.academy.bot.dto.telegramAPI;

public class TelegramMessage {
    private Long massageID;
    private TelegramChat chat;
    private String text;

    public Long getMassageID() {
        return massageID;
    }

    public void setMassageID(Long massageID) {
        this.massageID = massageID;
    }

    public TelegramChat getChat() {
        return chat;
    }

    public void setChat(TelegramChat chat) {
        this.chat = chat;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
