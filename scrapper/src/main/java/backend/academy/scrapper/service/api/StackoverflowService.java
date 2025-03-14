package backend.academy.scrapper.service.api;


import backend.academy.scrapper.client.StackoverflowClient;
import backend.academy.scrapper.dto.stackoverflow.QuestionAnswerDto;
import backend.academy.scrapper.dto.stackoverflow.QuestionDto;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.enums.LinkStatus;
import backend.academy.scrapper.service.LinkService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@RequiredArgsConstructor
@Service
public class StackoverflowService {

    private static final String QUESTION_ANSWER_URL = "https://stackoverflow.com/a/%s";
    private final StackoverflowClient stackoverflowClient;
    private final LinkService linkService;

    public Optional<String> getQuestionResponse(String id, Link link) {
        return Optional.of(stackoverflowClient.getQuestion(id))
            .map(QuestionDto::questions)
            .flatMap(questions -> {
                if (CollectionUtils.isEmpty(questions)) {
                    linkService.updateLinkStatus(link, LinkStatus.BROKEN);
                    return Optional.empty();
                } else {
                    return Optional.of(questions.getFirst());
                }
            })
            .filter(question -> question.updatedAt().isAfter(link.checkedAt()))
            .map(this::getQuestionResponseMessage);
    }

    public Optional<String> getQuestionAnswersResponse(String id, OffsetDateTime lastCheckedAt) {
        return Optional.of(stackoverflowClient.getQuestionAnswers(id))
            .map(QuestionAnswerDto::answers)
            .filter(answers -> !CollectionUtils.isEmpty(answers))
            .map(answers -> answers.stream()
                .filter(ans -> ans.updatedAt().isAfter(lastCheckedAt))
                .collect(Collectors.toList()))
            .map(this::getQuestionAnswersResponseMessage);
    }

    private String getQuestionResponseMessage(QuestionDto.Question question) {
        return String.format("✔ question [%s] was updated", question.title());
    }

    private String getQuestionAnswersResponseMessage(List<QuestionAnswerDto.Answer> answers) {
        return answers.stream()
            .map(answer -> String.format("➜ " + QUESTION_ANSWER_URL, answer.id()))
            .collect(Collectors.joining("\n"));
    }
}
