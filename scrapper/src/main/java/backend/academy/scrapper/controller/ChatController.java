package backend.academy.scrapper.controller;

import backend.academy.scrapper.dto.ApiErrorResponse;
import backend.academy.scrapper.repository.InMemoryChatRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tg-chat")
public class ChatController {
    private final InMemoryChatRepository chatRepository;

    public ChatController(InMemoryChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    @PostMapping("/{id}")
    public ResponseEntity<?> registerChat(@PathVariable("id") Long chatId) {
        if (chatId == null) {
            return ResponseEntity.badRequest().body(error("ChatId не указан"));
        }
        chatRepository.registerChat(chatId);
        return ResponseEntity.ok("Чат зарегистрирован");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteChat(@PathVariable("id") Long chatId) {
        if (chatId == null) {
            return ResponseEntity.badRequest().body(error("ChatId не указан"));
        }
        boolean removed = chatRepository.removeChat(chatId);
        if (!removed) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error("Чат не существует"));
        }
        return ResponseEntity.ok("Чат успешно удалён");
    }

    private ApiErrorResponse error(String message) {
        ApiErrorResponse error = new ApiErrorResponse();
        error.setDescription(message);
        error.setCode("400");
        return error;
    }
}
