package backend.academy.scrapper.service.jdbc;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import backend.academy.scrapper.configuration.DatabaseAccessConfig;
import backend.academy.scrapper.dto.bot.response.LinkResponse;
import backend.academy.scrapper.dto.bot.response.ListLinksResponse;
import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.enums.LinkStatus;
import backend.academy.scrapper.exception.ApiExceptionType;
import backend.academy.scrapper.repository.ChatLinkRepository;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.util.LinkParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Conditional(DatabaseAccessConfig.JdbcOrJooqAccessConfig.class)
@Service
public class JdbcLinkService implements LinkService {

    private final LinkRepository linkRepository;
    private final ChatLinkRepository chatLinkRepository;
    private final JdbcChatService chatService;

    @Override
    public List<Link> getLinksToUpdate(Integer minutes, Integer limit) {
        return linkRepository.findAllWithStatusAndOlderThan(
            LinkStatus.ACTIVE,
            OffsetDateTime.now().minusMinutes(minutes),
            limit
        );
    }

    @Transactional
    @Override
    public void updateLinkStatus(Link link, LinkStatus status) {
        log.debug("link{id={}} status was changed to {}", link.id(), status.name());
        linkRepository.updateStatus(link, status);
    }

    @Transactional
    @Override
    public void updateCheckedAt(Link link, OffsetDateTime checkedAt) {
        log.debug("link{id={}} was updated at {}", link.id(), checkedAt);
        linkRepository.updateCheckedAt(link, checkedAt);
    }

    @Transactional
    @Override
    public LinkResponse addLinkToChat(Long chatId, String url) {
        Link parsedLink = LinkParser.parseLink(url);
        Chat chat = chatService.findByChatId(chatId);
        Link link = processLinkForAdding(parsedLink, chat);
        chatLinkRepository.addLinkToChat(chat, link);
        log.debug("add link{id={}} to chat{id={}}", link.id(), chat.id());
        return new LinkResponse(link.id(), link.url());
    }

    @Transactional
    @Override
    public LinkResponse removeLinkFromChat(Long chatId, String url) {
        Link parsedLink = LinkParser.parseLink(url);
        Chat chat = chatService.findByChatId(chatId);
        Link link = processLinkForDeletion(parsedLink, chat);
        chatLinkRepository.removeLinkFromChat(chat, link);
        log.debug("remove link{id={}} from chat{id={}}", link.id(), chat.id());
        return new LinkResponse(link.id(), link.url());
    }

    @Override
    public ListLinksResponse getChatLinks(Long chatId) {
        Chat chat = chatService.findByChatId(chatId);
        List<LinkResponse> trackedLinks =
            linkRepository.findAllByChat(chat).stream()
                .map(link -> new LinkResponse(link.id(), link.url()))
                .toList();
        return new ListLinksResponse(trackedLinks, trackedLinks.size());
    }

    private Link processLinkForAdding(Link parsedLink, Chat chat) {
        return linkRepository.findByUrl(parsedLink.url())
            .filter(it -> canLinkBeAdded(it, chat))
            .orElseGet(() -> {
                Link saved = linkRepository.save(parsedLink);
                log.debug("new link{id={}} was saved", saved.id());
                return saved;
            });
    }

    private Link processLinkForDeletion(Link parsedLink, Chat chat) {
        return linkRepository.findByUrl(parsedLink.url())
            .filter(it -> chatLinkRepository.isLinkAddedToChat(chat, it))
            .orElseThrow(() -> ApiExceptionType.LINK_NOT_FOUND.toException(parsedLink.url()));
    }

    private boolean canLinkBeAdded(Link link, Chat chat) {
        if (chatLinkRepository.isLinkAddedToChat(chat, link)) {
            throw ApiExceptionType.LINK_ALREADY_EXISTS.toException(link.url());
        }
        return true;
    }
}
