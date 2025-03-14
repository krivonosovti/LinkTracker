package backend.academy.scrapper.service;

import backend.academy.scrapper.configuration.ScrapperConfig;
import backend.academy.scrapper.dto.bot.response.LinkUpdate;
import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.enums.LinkStatus;
import backend.academy.scrapper.handler.LinkUpdateHandler;
import backend.academy.scrapper.service.sender.UpdateSender;
import backend.academy.scrapper.util.LinkSourceUtil;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Component
public class LinkUpdaterService {

    @Value("${app.link-update-batch-size}")
    private Integer batchSize;
    @Value("${app.link-age}")
    private Integer linkAgeInMinutes;
    private final LinkService linkService;
    private final UpdateSender updateSender;
    private final Map<String, LinkUpdateHandler> linkUpdateHandlers;

    public LinkUpdaterService(
        LinkService linkService, UpdateSender updateSender,
        List<LinkUpdateHandler> linkUpdateHandlers
    ) {
        this.linkService = linkService;
        this.updateSender = updateSender;
        this.linkUpdateHandlers =
            linkUpdateHandlers.stream()
                .collect(Collectors.toMap(
                     it -> it.getClass().getCanonicalName(),
                    Function.identity()
                ));
    }

    @Transactional
    public void updateLinks() {
        List<Link> updates = linkService.getLinksToUpdate(linkAgeInMinutes, batchSize);
        updates.forEach(this::processLinkUpdate);
    }

    private void processLinkUpdate(Link link) {
        Optional<LinkUpdateHandler> handler = LinkSourceUtil.getLinkSource(link.linkType())
            .flatMap(it -> getLinkUpdateHandler(link, it));
        if (handler.isEmpty()) {
            log.warn("no update handler: LinkType={}, link=[{}]", link.linkType(), link.url());
            return;
        }
        OffsetDateTime checkedAt = OffsetDateTime.now();
        try {
            handler.get().getLinkUpdate(link)
                .ifPresentOrElse(
                    it ->
                        notifyBot(link, it, checkedAt),
                    () -> linkService.updateCheckedAt(link, checkedAt)
                );
        } catch (RuntimeException ex) {
            handleClientExceptionOnLinkUpdate(ex, link);
        }
    }

    private Optional<LinkUpdateHandler> getLinkUpdateHandler(Link link, ScrapperConfig.LinkSource linkSource) {
        return linkSource.handlers().values().stream()
                .filter(it -> Pattern.matches("https://" + linkSource.domain() + it.regex(), link.url()))
                .map(ScrapperConfig.LinkSourceHandler::handler)
                .map(linkUpdateHandlers::get)
                .findFirst();
    }

    private void notifyBot(Link link, String message, OffsetDateTime checkedAt) {
        LinkUpdate update = new LinkUpdate(
            link.id(),
            link.url(),
            message,
            link.chats().stream()
                .map(Chat::chatId)
                .collect(Collectors.toList())
        );
        log.debug(
            "send link update to the bot: id={}\nurl={}\nmessage={}",
            update.getId(),
            update.getUrl(),
            update.getDescription()
        );
        boolean isSent = updateSender.send(update);
        if (isSent) {
            linkService.updateCheckedAt(link, checkedAt);
        }
    }

    private void handleClientExceptionOnLinkUpdate(RuntimeException ex, Link link) {
        log.info("client error on link update: {}", ex.getMessage());
        if (ex instanceof WebClientResponseException clientExc) {
            HttpStatusCode status = clientExc.getStatusCode();
            if (status.equals(HttpStatus.NOT_FOUND) || status.equals(HttpStatus.BAD_REQUEST)) {
                linkService.updateLinkStatus(link, LinkStatus.BROKEN);
            }
        }
    }
}
