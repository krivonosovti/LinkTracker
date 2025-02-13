package backend.academy.bot.command;

import backend.academy.bot.service.TelegramClient;
import org.springframework.stereotype.Component;

@Component("unexpectedCommandHandler")
public class UnexpectedCommandHandler implements CommandHandler {

    private final TelegramClient telegramClient;

    public UnexpectedCommandHandler(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    /**
     * Метод возвращает специальное название команды, по которому она будет исключаться из основного маппинга.
     */
    @Override
    public String getCommand() {
        return "unexpected";
    }

    /**
     * Обработка неизвестной команды.
     *
     * @param chatId      идентификатор чата
     * @param messageText полный текст сообщения пользователя
     */
    @Override
    public void handle(Long chatId, String messageText) {
        // Формирование списка доступных команд.
        // Здесь можно либо использовать статический список, либо получить его динамически.
        String availableCommands = "/help - список команд\n" +
            "/track - начать отслеживание ссылки\n" +
            "/untrack - прекратить отслеживание ссылки\n" +
            "/list - показать список отслеживаемых ссылок";
        String response = "Неизвестная команда. Доступные команды:\n" + availableCommands;

        // Отправка ответа пользователю через TelegramClient.
        telegramClient.sendMessage(chatId, response);
    }
}
