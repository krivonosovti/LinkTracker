package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.dto.ListLinksResponse;
import backend.academy.scrapper.dto.RemoveLinkRequest;
import backend.academy.scrapper.repository.InMemoryLinkRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LinkService {

    private final InMemoryLinkRepository linkRepository;

    public LinkService(InMemoryLinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    public LinkResponse addLink(Long tgChatId, AddLinkRequest request) {
       return linkRepository.addLink(tgChatId, request);
    }

    public LinkResponse removeLink(Long tgChatId, RemoveLinkRequest request) {
        return linkRepository.removeLink(tgChatId, request.getLink());
    }
}
