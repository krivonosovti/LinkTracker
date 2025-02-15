package backend.academy.bot.command;

public interface CommandHandler {
    /**
     * Возвращает команду, которую обрабатывает данный обработчик (например, "/start")
     */
    String getCommand();

    /**
     * Обработка команды для конкретного chatId
     */
    void handle(Long chatId, String messageText);
}

