package backend.academy.scrapper.dto.request;

import jakarta.validation.constraints.NotNull;
import java.net.URI;

public record RemoveLinkRequest(@NotNull URI link) {
}
