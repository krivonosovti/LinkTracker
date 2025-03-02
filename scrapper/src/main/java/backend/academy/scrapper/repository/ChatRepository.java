package backend.academy.scrapper.repository;


import backend.academy.scrapper.entity.Chat;

import java.util.Optional;

public interface ChatRepository {

    Chat save(Chat chat);

    boolean delete(Long chatId);

    Optional<Chat> findByChatId(Long chatId);

    boolean existsByChatId(Long chatId);

    Optional<Chat> findWithLinksByChatId(Long chatId);
}
