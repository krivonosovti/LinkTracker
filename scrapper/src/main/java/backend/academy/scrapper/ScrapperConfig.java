package backend.academy.scrapper;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
@EnableScheduling
//public record ScrapperConfig(@NotEmpty String githubToken, StackOverflowCredentials stackOverflow) {
//    public record StackOverflowCredentials(@NotEmpty String key, @NotEmpty String accessToken) {}
//}
public record ScrapperConfig(String githubToken, StackOverflowCredentials stackOverflow) {
    public record StackOverflowCredentials(String key,  String accessToken) {}
}
