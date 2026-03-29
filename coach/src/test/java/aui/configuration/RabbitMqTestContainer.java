package aui.configuration;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.testcontainers.containers.RabbitMQContainer;

import static java.lang.String.join;
import static java.util.Collections.singletonList;

public class RabbitMqTestContainer {

    private static final String DOCKER_IMAGE_NAME = "rabbitmq";
    private static final String DEFAULT_AMQP_PORT = "5672";

    protected static final RabbitMQContainer RABBIT_MQ_CONTAINER;

    static {
        RABBIT_MQ_CONTAINER = new RabbitMQContainer(DOCKER_IMAGE_NAME);
        RABBIT_MQ_CONTAINER.setPortBindings(singletonList(join(":", DEFAULT_AMQP_PORT, DEFAULT_AMQP_PORT)));
    }

    public RabbitMqTestContainer() {
        RABBIT_MQ_CONTAINER.start();
    }

    public ConnectionFactory createConnectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(RABBIT_MQ_CONTAINER.getHost());
        connectionFactory.setUsername(RABBIT_MQ_CONTAINER.getAdminUsername());
        connectionFactory.setPassword(RABBIT_MQ_CONTAINER.getAdminPassword());
        connectionFactory.setPort(RABBIT_MQ_CONTAINER.getAmqpPort());

        return connectionFactory;
    }
}
