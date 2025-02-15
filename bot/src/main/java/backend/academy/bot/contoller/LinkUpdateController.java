package backend.academy.bot.contoller;

import backend.academy.bot.dto.ApiErrorResponse;
import backend.academy.bot.dto.LinkUpdate;
import backend.academy.bot.state.StateMachine;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/updates")
@Validated
public class LinkUpdateController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LinkUpdateController.class);
    private final StateMachine stateMachine;

    public LinkUpdateController(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    @PostMapping
    public ResponseEntity<Void> receiveUpdate(@Valid @RequestBody LinkUpdate update) {
        LOGGER.info("Получено обновление: {}", update);
        for (Long chatId : update.getTgChatIds()) {
            stateMachine.notifyUser(chatId, update);
        }
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        ApiErrorResponse errorResponse = new ApiErrorResponse(
            "Некорректные параметры запроса",
            "400",
            ex.getClass().getSimpleName(),
            ex.getMessage(),
            List.of(ex.getStackTrace()).toString()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
