package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.ExternalResourceUpdate;
import reactor.core.publisher.Mono;

public interface ChangeApiClient {
    Mono<ExternalResourceUpdate>  getUpdate(String url);
}
