package edu.java.client;

import backend.academy.scrapper.client.StackoverflowClient;
import backend.academy.scrapper.dto.stackoverflow.QuestionAnswerDto;
import backend.academy.scrapper.dto.stackoverflow.QuestionDto;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ExtendWith(MockitoExtension.class)
class StackoverflowClientTest {

    private StackoverflowClient stackoverflowClient;
    @RegisterExtension
    private static final WireMockExtension wireMockExtension = WireMockExtension.newInstance()
        .options(wireMockConfig().dynamicPort())
        .build();

    private static final String API_QUESTION = "/questions/.*?.*";
    private static final String API_QUESTION_ANSWERS = "/questions/.*/answers?.*";

    @BeforeEach
    void init() {
        stackoverflowClient = HttpServiceProxyFactory
            .builderFor(
                WebClientAdapter.create(
                    WebClient.builder().baseUrl(wireMockExtension.baseUrl()).build()
                ))
            .build()
            .createClient(StackoverflowClient.class);
    }

    @Nested
    class GetRequestTest {

        @SneakyThrows
        @Test
        void getQuestionTest() {
            String response = new String(Files.readAllBytes(
                Paths.get(ClassLoader.getSystemResource("stackoverflow/question_response.json").toURI())
            ));
            OffsetDateTime expectedUpdatedAt = OffsetDateTime.of(
                LocalDate.of(2024, 2, 20),
                LocalTime.of(8, 19, 24),
                ZoneOffset.UTC
            );
            wireMockExtension.stubFor(
                get(urlMatching(API_QUESTION))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(response)
                    )
            );

            QuestionDto questionRes = stackoverflowClient.getQuestion("24840667");

            assertThat(questionRes.questions().size()).isEqualTo(1);
            QuestionDto.Question question = questionRes.questions().get(0);
            assertThat(question.title()).isEqualTo("What is the regex to extract all the emojis from a string?");
            assertThat(question.updatedAt()).isEqualTo(expectedUpdatedAt);
        }

        @SneakyThrows
        @Test
        void getQuestionAnswersTest() {
            String response = new String(Files.readAllBytes(
                Paths.get(ClassLoader.getSystemResource("stackoverflow/answers_response.json").toURI())
            ));
            List<String> expectedAnswerIds = List.of("24841069", "75528528", "77157680", "67730175");
            wireMockExtension.stubFor(
                get(urlMatching(API_QUESTION_ANSWERS))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(response)
                    )
            );

            QuestionAnswerDto answersRes = stackoverflowClient.getQuestionAnswers("24840667");

            assertThat(answersRes.answers().size()).isEqualTo(4);
            List<String> answersIds = answersRes.answers().stream()
                .map(QuestionAnswerDto.Answer::id)
                .toList();
            assertThat(answersIds).isEqualTo(expectedAnswerIds);
        }

        @SneakyThrows
        @Test
        void response400Test() {
            wireMockExtension.stubFor(
                get(urlMatching(API_QUESTION))
                    .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody("""
                            {
                                "error_id": 400,
                                "error_message": "ids",
                                "error_name": "bad_parameter"
                            }
                            """)
                    )
            );

            WebClientResponseException ex = catchThrowableOfType(
                () -> stackoverflowClient.getQuestion("24840667"),
                WebClientResponseException.class
            );
            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }
}
