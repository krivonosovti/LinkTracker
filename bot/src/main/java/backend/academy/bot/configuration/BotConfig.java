package backend.academy.bot.configuration;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record BotConfig(
    @NotEmpty
    String telegramToken,
    @NotNull
    ScrapperClient scrapperClient
) {
    public record ScrapperClient(@NotNull String api) {
    }
}



