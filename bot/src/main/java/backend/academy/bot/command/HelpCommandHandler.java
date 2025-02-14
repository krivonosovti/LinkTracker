package backend.academy.bot.command;

import backend.academy.bot.service.TelegramClient;
import backend.academy.bot.state.StateMachine;
import org.springframework.stereotype.Component;
import java.util.logging.Logger;

@Component
public class HelpCommandHandler implements CommandHandler {

    private final TelegramClient telegramClient;

    public HelpCommandHandler(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    @Override
    public String getCommand() {
        return "/help";
    }

    @Override
    public void handle(Long chatId, String messageText) {
        String helpMessage = "Доступные команды:\n" +
            "/help - список команд\n" +
            "/track - начать отслеживание ссылки\n" +
            "/untrack - прекратить отслеживание ссылки\n" +
            "/list - показать список отслеживаемых ссылок";
        telegramClient.sendMessage(chatId, helpMessage);
    }
}
