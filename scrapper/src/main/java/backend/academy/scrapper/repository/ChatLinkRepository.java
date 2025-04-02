package backend.academy.scrapper.repository;


import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.entity.Link;

public interface ChatLinkRepository {

    boolean isLinkAddedToChat(Chat chat, Link link);

    boolean addLinkToChat(Chat chat, Link link);

    boolean removeLinkFromChat(Chat chat, Link link);
}
