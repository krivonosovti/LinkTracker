package backend.academy.scrapper.configuration;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class DatabaseAccessConfig {

    @Configuration
    @ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "jdbc")
    public static class JdbcAccessConfig {
        @PostConstruct
        private void init() {
            log.info("database access: jdbc");
        }
    }

//    @Configuration
//    @ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "jooq")
//    public static class JooqAccessConfig {
//        @PostConstruct
//        private void init() {
//            log.info("database access: jooq");
//        }
//    }

    @Configuration
    @ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "jpa")
    public static class JpaAccessConfig {
        @PostConstruct
        private void init() {
            log.info("database access: jpa");
        }
    }

    public static class JdbcOrJooqAccessConfig extends AnyNestedCondition {

        public JdbcOrJooqAccessConfig() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        @ConditionalOnBean(JdbcAccessConfig.class)
        private static class JdbcCondition {}
//
//        @ConditionalOnBean(JooqAccessConfig.class)
//        private static class JooqCondition {}
    }
}
