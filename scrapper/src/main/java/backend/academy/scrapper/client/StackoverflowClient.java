package backend.academy.scrapper.client;

import backend.academy.scrapper.dto.stackoverflow.QuestionAnswerDto;
import backend.academy.scrapper.dto.stackoverflow.QuestionDto;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(accept = MediaType.APPLICATION_JSON_VALUE)
public interface StackoverflowClient {

    @GetExchange("/questions/{id}?site=stackoverflow")
    QuestionDto getQuestion(@PathVariable String id);

    @GetExchange("/questions/{id}/answers?site=stackoverflow")
    QuestionAnswerDto getQuestionAnswers(@PathVariable String id);
}
