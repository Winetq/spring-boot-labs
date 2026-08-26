package aui.configuration;

import aui.SpringBootLabsApplication;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import static aui.configuration.RabbitMqTestContainer.RABBIT_MQ_CONTAINER;

@SpringBootTest(classes = SpringBootLabsApplication.class)
public class IntegrationTestConfiguration extends AbstractTestNGSpringContextTests {

    private static final String DELETE_COACH_QUEUE = "delete.coach.queue";
    private static final String GET_COACH_SWIMMERS_QUEUE = "get.coach.swimmers.queue";

    protected static final RabbitMqTestContainer RABBIT_MQ_TEST_CONTAINER = new RabbitMqTestContainer();

    static {
        AmqpAdmin amqpAdmin = new RabbitAdmin(RABBIT_MQ_TEST_CONTAINER.createConnectionFactory());

        amqpAdmin.declareQueue(new Queue(DELETE_COACH_QUEUE, true));
        amqpAdmin.declareQueue(new Queue(GET_COACH_SWIMMERS_QUEUE, true));
    }

    @DynamicPropertySource
    public static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:spring-boot-app");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.datasource.username", () -> "admin");
        registry.add("spring.datasource.password", () -> "admin");
        registry.add("spring.datasource-replica.url", () -> "jdbc:h2:mem:spring-boot-app");
        registry.add("spring.datasource-replica.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.datasource-replica.username", () -> "admin");
        registry.add("spring.datasource-replica.password", () -> "admin");
        registry.add("spring.rabbitmq.host", RABBIT_MQ_CONTAINER::getHost);
        registry.add("spring.rabbitmq.port", RABBIT_MQ_CONTAINER::getAmqpPort);
        registry.add("spring.rabbitmq.username", RABBIT_MQ_CONTAINER::getAdminUsername);
        registry.add("spring.rabbitmq.password", RABBIT_MQ_CONTAINER::getAdminPassword);
        registry.add("server.port", () -> 0); // random available port
    }
}
