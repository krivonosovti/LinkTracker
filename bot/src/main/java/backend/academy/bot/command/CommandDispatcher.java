package backend.academy.bot.command;

import backend.academy.bot.service.TelegramClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CommandDispatcher {
    private final Map<String, CommandHandler> commandHandlers = new HashMap<>();
    private final CommandHandler unexpectedCommandHandler;

    /**
     * Конструктор получает список всех CommandHandler-ов из контекста Spring.
     * Обработчик для неизвестных команд фильтруется и сохраняется отдельно.
     */
    public CommandDispatcher(List<CommandHandler> handlers,
                             @Qualifier("unexpectedCommandHandler") CommandHandler unexpectedCommandHandler) {
        for (CommandHandler handler : handlers) {
            // Не включаем unexpectedCommandHandler в основной маппинг
            if (!handler.getCommand().equalsIgnoreCase("unexpected")) {
                commandHandlers.put(handler.getCommand().toLowerCase(), handler);
            }
        }
        this.unexpectedCommandHandler = unexpectedCommandHandler;
    }

    public void dispatch(Long chatId, String messageText) {
        if (messageText == null || messageText.isBlank()) {
            return;
        }
        // Предполагаем, что команда — первое слово в сообщении
        String command = messageText.split(" ")[0].trim().toLowerCase();
        CommandHandler handler = commandHandlers.get(command);
        if (handler != null) {
            handler.handle(chatId, messageText);
        } else {
            // Вызываем специальный обработчик неизвестной команды
            unexpectedCommandHandler.handle(chatId, messageText);
        }
    }
}
