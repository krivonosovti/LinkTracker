package edu.java.util;

import backend.academy.scrapper.configuration.ScrapperConfig;
import backend.academy.scrapper.util.RetryUtil;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.time.Duration;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;

@ExtendWith(MockitoExtension.class)
class RetryUtilTest {

    @RegisterExtension
    private static final WireMockExtension wireMockExtension = WireMockExtension.newInstance()
        .options(wireMockConfig().dynamicPort())
        .build();

    private static final int MAX_ATTEMPTS = 2;
    private static final Duration BACKOFF = Duration.ofSeconds(1);
    private static final int RETRY_CODE = 429;

    @BeforeEach
    void init() {
        wireMockExtension.stubFor(
            get(urlMatching(".*"))
                .inScenario("retry")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse()
                    .withStatus(RETRY_CODE)
                )
                .willSetStateTo("1 attempt")
        );
        wireMockExtension.stubFor(
            get(urlMatching(".*"))
                .inScenario("retry")
                .whenScenarioStateIs("1 attempt")
                .willReturn(aResponse()
                    .withStatus(RETRY_CODE)
                )
                .willSetStateTo("2 attempt")
        );
    }

    @Nested
    class RetryAttemptsLessThanMaxTest {

        @SneakyThrows
        @ParameterizedTest
        @EnumSource(ScrapperConfig.RetryStrategy.class)
        void shouldReturnResponseWhenItDoesNotExceedMaxAttempts(ScrapperConfig.RetryStrategy retryStrategy) {
            wireMockExtension.stubFor(
                get(urlMatching(".*"))
                    .inScenario("retry")
                    .whenScenarioStateIs("2 attempt")
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("successful")
                    )
            );
            ScrapperConfig.RetryConfig retryConfig = new ScrapperConfig.RetryConfig(
                retryStrategy,
                MAX_ATTEMPTS,
                BACKOFF,
                null,
                List.of(RETRY_CODE)
            );
            WebClient webClient = WebClient.builder()
                .baseUrl(wireMockExtension.baseUrl())
                .filter(RetryUtil.retryFilter(retryConfig))
                .build();

            Mono<String> response = webClient.get()
                .retrieve()
                .bodyToMono(String.class);

            StepVerifier.create(response)
                .expectNext("successful")
                .verifyComplete();
        }
    }

    @Nested
    class RetryAttemptsGreaterThanMaxTest {

        @SneakyThrows
        @ParameterizedTest
        @EnumSource(ScrapperConfig.RetryStrategy.class)
        void shouldReturnResponseWhenItDoesNotExceedMaxAttempts(ScrapperConfig.RetryStrategy retryStrategy) {
            wireMockExtension.stubFor(
                get(urlMatching(".*"))
                    .inScenario("retry")
                    .whenScenarioStateIs("2 attempt")
                    .willReturn(aResponse()
                        .withStatus(RETRY_CODE)
                    )
            );
            ScrapperConfig.RetryConfig retryConfig = new ScrapperConfig.RetryConfig(
                retryStrategy,
                MAX_ATTEMPTS,
                BACKOFF,
                null,
                List.of(RETRY_CODE)
            );
            WebClient webClient = WebClient.builder()
                .baseUrl(wireMockExtension.baseUrl())
                .filter(RetryUtil.retryFilter(retryConfig))
                .build();

            Mono<String> response = webClient.get()
                .retrieve()
                .bodyToMono(String.class);

            StepVerifier.create(response)
                .expectError(WebClientResponseException.TooManyRequests.class)
                .verify();
        }
    }
}
