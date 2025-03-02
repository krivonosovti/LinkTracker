package backend.academy.scrapper.service.sender;


import backend.academy.scrapper.dto.response.LinkUpdate;

public interface UpdateSender {

    boolean send(LinkUpdate update);
}
