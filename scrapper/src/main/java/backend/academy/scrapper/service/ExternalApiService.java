package backend.academy.scrapper.service;

import backend.academy.scrapper.ScrapperConfig;
import org.springframework.stereotype.Service;

@Service
public class ExternalApiService {
    private final ScrapperConfig scrapperConfig;

    public ExternalApiService(ScrapperConfig scrapperConfig) {
        this.scrapperConfig = scrapperConfig;
    }

    public void example() {
        // Пример использования токена GitHub
        String githubToken = scrapperConfig.githubToken();
        // Или доступ к данным StackOverflow:
        String soKey = scrapperConfig.stackOverflow().key();
        String soAccessToken = scrapperConfig.stackOverflow().accessToken();
        // Используйте данные для вызова внешних API
    }
}
