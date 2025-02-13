package backend.academy.bot.command;

import backend.academy.bot.service.TelegramClient;

public class UpdatesCommandHandler implements CommandHandler{
    private final TelegramClient telegramClient;

    public UpdatesCommandHandler(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    @Override
    public String getCommand() {
        return "/updates";
    }

    @Override
    public void handle(Long chatId, String messageText) {  // тут нужно прериписать с выводом обновления 
        String helpMessage = "Доступные команды:\n" +
            "/help - список команд\n" +
            "/track - начать отслеживание ссылки\n" +
            "/untrack - прекратить отслеживание ссылки\n" +
            "/list - показать список отслеживаемых ссылок";
        telegramClient.sendMessage(chatId, helpMessage);
    }
}
