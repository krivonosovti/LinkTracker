package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.Chat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryChatRepository implements ChatRepository {

    private final Map<Long, Chat> chatStorage = new HashMap<>();

    @Override
    public Chat save(Chat chat) {
        chatStorage.put(chat.chatId(), chat);
        return chat;
    }

    @Override
    public boolean delete(Long chatId) {
        return chatStorage.remove(chatId) != null;
    }

    @Override
    public Optional<Chat> findByChatId(Long chatId) {
        return Optional.ofNullable(chatStorage.get(chatId));
    }

    @Override
    public boolean existsByChatId(Long chatId) {
        return chatStorage.containsKey(chatId);
    }

    @Override
    public Optional<Chat> findWithLinksByChatId(Long chatId) {
        Chat chat = chatStorage.get(chatId);
        return chat != null ? Optional.of(chat) : Optional.empty();
    }
}
