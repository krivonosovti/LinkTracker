package backend.academy.scrapper.repository;

import org.springframework.stereotype.Repository;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

@Repository
public class InMemoryChatRepository {
    private final Set<Long> chats = new ConcurrentSkipListSet<>();

    public void registerChat(Long chatId) {
        chats.add(chatId);
    }

    public boolean removeChat(Long chatId) {
        return chats.remove(chatId);
    }

    public boolean exists(Long chatId) {
        return chats.contains(chatId);
    }
}
