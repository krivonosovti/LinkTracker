package backend.academy.scrapper.service.sender;


import backend.academy.scrapper.dto.bot.response.LinkUpdate;

public interface UpdateSender {

    boolean send(LinkUpdate update);
}
