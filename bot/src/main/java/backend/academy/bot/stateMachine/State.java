package backend.academy.bot.stateMachine;

public enum State {
    START,               // До получения команды /start
    COMMAND_WAITING,     // Ожидание команды (/help, /track, /list, /untrack)
    HANDLS,              // Обработка команд (не track)
    ADD_LINK,            // Ожидание ввода ссылки для /track (если ссылка не передана сразу)
    ASK_ABT_TAG,
    WAITING_FOR_TAGS,
    WAITING_FOR_FILTERS,
}
