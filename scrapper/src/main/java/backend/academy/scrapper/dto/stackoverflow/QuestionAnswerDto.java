package backend.academy.scrapper.dto.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;

public record QuestionAnswerDto(@JsonProperty("items") List<Answer> answers) {

    public record Answer(@JsonProperty("last_activity_date") OffsetDateTime updatedAt,
                         @JsonProperty("answer_id") String id,
                         @JsonProperty("question_id") String questionId) {
    }
}
