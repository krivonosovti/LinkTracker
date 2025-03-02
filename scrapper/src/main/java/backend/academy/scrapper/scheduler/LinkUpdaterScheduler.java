package backend.academy.scrapper.scheduler;

import backend.academy.scrapper.service.LinkUpdaterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = {"app.link-updater-scheduler.enable"}, havingValue = "true")
public class LinkUpdaterScheduler {

    private final LinkUpdaterService linkUpdaterService;

    @Scheduled(initialDelayString = "${app.link-updater-scheduler.force-check-delay}",
               fixedDelayString = "${app.link-updater-scheduler.interval}")
    public void update() {
        log.debug("the link update task has been started");
        linkUpdaterService.updateLinks();
    }
}
