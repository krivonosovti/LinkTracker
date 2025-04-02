package backend.academy.scrapper.repository.jpa;


import java.util.Optional;
import backend.academy.scrapper.configuration.DatabaseAccessConfig;
import backend.academy.scrapper.entity.Chat;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@ConditionalOnBean(DatabaseAccessConfig.JpaAccessConfig.class)
@Repository
public interface JpaChatRepository extends JpaRepository<Chat, Long> {

    Optional<Chat> findByChatId(Long chatId);

    boolean existsByChatId(Long chatId);

    @EntityGraph(attributePaths = "links")
    Optional<Chat> findWithLinksByChatId(Long chatId);
}
