package backend.academy.scrapper.configuration;

import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.client.GithubClient;
import backend.academy.scrapper.client.StackoverflowClient;
import backend.academy.scrapper.util.RetryUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@RequiredArgsConstructor
@Component
public class ClientConfig {

    private final ScrapperConfig applicationConfig;

    @Bean
    public GithubClient githubClient() {
        return buildHttpInterface(applicationConfig.githubClient().api(), GithubClient.class,
            applicationConfig.githubClient().retry());
    }

    @Bean
    public StackoverflowClient stackoverflowClient() {
        return buildHttpInterface(applicationConfig.stackoverflowClient().api(), StackoverflowClient.class,
            applicationConfig.stackoverflowClient().retry());
    }

    @Bean
    public BotClient botClient() {
        return buildHttpInterface(applicationConfig.botClient().api(), BotClient.class,
            applicationConfig.botClient().retry());
    }

    private <T> T buildHttpInterface(String baseUrl, Class<T> serviceType, ScrapperConfig.RetryConfig retryConfig) {
        WebClientAdapter webClientAdapter = WebClientAdapter.create(
            WebClient.builder()
                .baseUrl(baseUrl)
                .filter(RetryUtil.retryFilter(retryConfig))
                .build());
        return HttpServiceProxyFactory.builderFor(webClientAdapter).build().createClient(serviceType);
    }
}
