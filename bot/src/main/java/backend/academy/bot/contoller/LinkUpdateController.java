package backend.academy.bot.contoller;

import backend.academy.bot.dto.ApiErrorResponse;
import backend.academy.bot.dto.LinkUpdate;
import backend.academy.bot.state.StateMachine;
import jakarta.validation.Valid;
import java.util.ArrayList;
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
    private static final String BAD_REQUEST_DESCRIPTION = "Некорректные параметры запроса";
    private static final String BAD_REQUEST_CODE = "400";
    private static final String BAD_REQUEST_EXCEPTION = "IllegalArgumentException";
    private static final String BAD_REQUEST_MESSAGE = "Chat ID не должен быть null";

    private final StateMachine stateMachine;

    public LinkUpdateController(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    @PostMapping
    public ResponseEntity<?> receiveUpdate(@Valid @RequestBody LinkUpdate update) {
        if (!update.isEmpty()) {
            try {
                LOGGER.info("Получено обновление: {}", update);
                List<Long> chatStorage = update.getTgChatIds();
                for (Long chatId : chatStorage) {
                    stateMachine.notifyUser(chatId, update);
                }
                return ResponseEntity.ok().build();
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(
                    createBadRequestError(BAD_REQUEST_DESCRIPTION, new ArrayList<>()));
            }
        } else {
            return ResponseEntity.badRequest().body(
                createBadRequestError(BAD_REQUEST_DESCRIPTION, new ArrayList<>()));
        }
    }

//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
//        ApiErrorResponse errorResponse = new ApiErrorResponse(
//            "Некорректные параметры запроса",
//            "400",
//            ex.getClass().getSimpleName(),
//            ex.getMessage(),
//            List.of(ex.getStackTrace().)
//        );
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
//    }

    private ApiErrorResponse createBadRequestError(String description, List<String> stacktrace) {
        return new ApiErrorResponse(description, BAD_REQUEST_CODE, BAD_REQUEST_EXCEPTION,
            BAD_REQUEST_MESSAGE, stacktrace);
    }
}
