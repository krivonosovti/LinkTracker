package backend.academy.scrapper.service.jpa;

import backend.academy.scrapper.configuration.DatabaseAccessConfig;
import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.exception.ApiExceptionType;
import backend.academy.scrapper.repository.jpa.JpaChatRepository;
import backend.academy.scrapper.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@ConditionalOnBean(DatabaseAccessConfig.JpaAccessConfig.class)
@Service
public class JpaChatService implements ChatService {

    private final JpaChatRepository chatRepository;

    @Override
    public Chat findByChatId(Long chatId) {
        return chatRepository.findWithLinksByChatId(chatId)
            .orElseThrow(() -> ApiExceptionType.CHAT_NOT_FOUND.toException(chatId));
    }

    @Transactional
    @Override
    public void registerChat(Long chatId) {
        if (chatRepository.existsByChatId(chatId)) {
            throw ApiExceptionType.CHAT_ALREADY_EXISTS.toException(chatId);
        }
        Chat chat = Chat.builder().chatId(chatId).build();
        chatRepository.save(chat);
        log.debug("new chat{chatId={}} was registered", chatId);
    }

    @Transactional
    @Override
    public void deleteChat(Long chatId) {
        Chat chat = chatRepository.findByChatId(chatId)
            .orElseThrow(() -> ApiExceptionType.CHAT_NOT_FOUND.toException(chatId));
        chatRepository.delete(chat);
        log.debug("chat{chatId={}} was deleted", chatId);
    }
}
