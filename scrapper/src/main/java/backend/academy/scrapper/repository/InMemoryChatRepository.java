package backend.academy.scrapper.repository;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryChatRepository {
    private final Set<Long> chats = new ConcurrentSkipListSet<>();
    Logger logger = Logger.getLogger(InMemoryChatRepository.class.getName());

    public void registerChat(Long chatId) {
        if (!chats.add(chatId)) {
            logger.log(Level.INFO, "This chat id is already registered");
        }
    }

    public boolean removeChat(Long chatId) {
        return chats.remove(chatId);
    }

    public boolean exists(Long chatId) {
        return chats.contains(chatId);
    }
}
