package aui.rabbitmq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CoachPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.queue.delete-coach-queue}")
    private String deleteCoachQueue;

    @Value("${spring.rabbitmq.queue.get-coach-swimmers-queue}")
    private String getCoachSwimmersQueue;

    public void publishDeleteCoachEvent(Long coachId) { // asynchronous (Event Pattern)
        log.info("CoachPublisher publishes DeleteCoachEvent with id: {}", coachId);
        rabbitTemplate.convertAndSend(deleteCoachQueue, new DeleteCoachEvent(coachId));
    }

    public List<GetCoachSwimmersResponse> publishGetCoachSwimmersRequest(Long coachId) { // synchronous (Request-Response Pattern)
        log.info("CoachPublisher publishes GetCoachSwimmersRequest with id: {}", coachId);
        return rabbitTemplate.convertSendAndReceiveAsType(
                getCoachSwimmersQueue,
                new GetCoachSwimmersRequest(coachId),
                new ParameterizedTypeReference<>() {}
        );
    }
}
