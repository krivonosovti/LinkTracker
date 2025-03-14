package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.bot.response.LinkResponse;
import backend.academy.scrapper.dto.bot.response.ListLinksResponse;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.enums.LinkStatus;
import java.time.OffsetDateTime;
import java.util.List;

public interface LinkService {

    List<Link> getLinksToUpdate(Integer minutes, Integer limit);

    void updateLinkStatus(Link link, LinkStatus status);

    void updateCheckedAt(Link link, OffsetDateTime checkedAt);

    LinkResponse addLinkToChat(Long chatId, String url);

    LinkResponse removeLinkFromChat(Long chatId, String url);

    ListLinksResponse getChatLinks(Long chatId);
}
