package aui.configuration;

import aui.SpringBootLabsApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import static aui.configuration.PostgreSqlTestContainer.POSTGRES_CONTAINER;
import static aui.configuration.RabbitMqTestContainer.RABBIT_MQ_CONTAINER;

@SpringBootTest(classes = SpringBootLabsApplication.class)
public class IntegrationTestConfiguration extends AbstractTestNGSpringContextTests {

    protected static final RabbitMqTestContainer RABBIT_MQ_TEST_CONTAINER = new RabbitMqTestContainer();
    protected static final PostgreSqlTestContainer POSTGRE_SQL_TEST_CONTAINER = new PostgreSqlTestContainer();

    @DynamicPropertySource
    public static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        registry.add("spring.rabbitmq.host", RABBIT_MQ_CONTAINER::getHost);
        registry.add("spring.rabbitmq.port", RABBIT_MQ_CONTAINER::getAmqpPort);
        registry.add("spring.rabbitmq.username", RABBIT_MQ_CONTAINER::getAdminUsername);
        registry.add("spring.rabbitmq.password", RABBIT_MQ_CONTAINER::getAdminPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none"); // init.sql handles schema and data
        registry.add("server.port", () -> 0); // random available port
    }
}
