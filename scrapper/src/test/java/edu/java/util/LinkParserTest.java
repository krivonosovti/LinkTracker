package edu.java.util;

import java.net.URI;
import java.util.stream.Stream;
import backend.academy.scrapper.configuration.ScrapperConfig;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.enums.LinkType;
import backend.academy.scrapper.exception.ApiException;
import backend.academy.scrapper.util.LinkParser;
import backend.academy.scrapper.util.LinkSourceUtil;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

@SpringBootTest(classes = {LinkSourceUtil.class})
@EnableConfigurationProperties(value = ScrapperConfig.class)
class LinkParserTest {

    @Nested
    class ParseLinkTest {

        @ParameterizedTest
        @MethodSource("edu.java.util.LinkParserTest#validLink")
        void shouldReturnParsedLinkWhenLinkIsValid(String url, LinkType linkType) {
            Link actual = LinkParser.parseLink(url);

            assertThat(actual.url()).isEqualTo(url);
            assertThat(actual.linkType()).isEqualTo(linkType);
        }

        @ParameterizedTest
        @MethodSource("edu.java.util.LinkParserTest#invalidLink")
        void shouldThrowExceptionWhenLinkIsInvalid(String url) {
            ApiException ex = catchThrowableOfType(
                () -> LinkParser.parseLink(url),
                ApiException.class
            );
            assertThat(ex.code()).isEqualTo("INVALID_LINK");
            assertThat(ex.messageProp()).isEqualTo("ex.api.invalidLink");
            assertThat(ex.messageArgs().length).isEqualTo(0);
            assertThat(ex.status()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @ParameterizedTest
        @MethodSource("edu.java.util.LinkParserTest#unsupportedLink")
        void shouldThrowExceptionWhenLinkIsNotSupported(String url) {
            ApiException ex = catchThrowableOfType(
                () -> LinkParser.parseLink(url),
                ApiException.class
            );
            assertThat(ex.code()).isEqualTo("NOT_SUPPORTED_SOURCE");
            assertThat(ex.messageProp()).isEqualTo("ex.api.notSupportedSource");
            assertThat(ex.messageArgs().length).isEqualTo(0);
            assertThat(ex.status()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    static Stream<Arguments> validLink() {
        return Stream.of(
            Arguments.of("https://github.com/JetBrains/kotlin", LinkType.GITHUB),
            Arguments.of("https://github.com/JetBrains/kotlin/tree/branch-name", LinkType.GITHUB),
            Arguments.of("https://github.com/JetBrains/kotlin/pull/1", LinkType.GITHUB),
            Arguments.of("https://github.com/JetBrains/kotlin/issues/1", LinkType.GITHUB),
            Arguments.of("https://stackoverflow.com/questions/24840667", LinkType.STACKOVERFLOW),
            Arguments.of("https://stackoverflow.com/q/24840667", LinkType.STACKOVERFLOW),
            Arguments.of("https://stackoverflow.com/questions/24840667/question-title", LinkType.STACKOVERFLOW)
        );
    }

    static Stream<Arguments> invalidLink() {
        return Stream.of(
            Arguments.of(""),
            Arguments.of("dummy"),
            Arguments.of("github.com/JetBrains/kotlin"),
            Arguments.of("http:/github.com/JetBrains/kotlin"),
            Arguments.of("https://stackoverflow.com/search!q=exception")
        );
    }

    static Stream<Arguments> unsupportedLink() {
        return Stream.of(
            Arguments.of("https://www.baeldung.com/mockito-series"),
            Arguments.of("https://leetcode.com/problemset/algorithms/"),
            Arguments.of("https://github.com"),
            Arguments.of("https://stackoverflow.com"),
            Arguments.of("https://github.com/JetBrains/kotlin/releases"),
            Arguments.of("https://github.com/JetBrains/kotlin/stargazers"),
            Arguments.of("https://github.com/JetBrains/kotlin/pulls"),
            Arguments.of("https://github.com/JetBrains/kotlin/issues"),
            Arguments.of("https://stackoverflow.com/a/32872406"),
            Arguments.of("https://stackoverflow.com/questions/24840667#32872406")
        );
    }
}
