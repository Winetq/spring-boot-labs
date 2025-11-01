package aui.rabbitmq;

import aui.swimmer.Swimmer;
import aui.swimmer.SwimmerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CoachConsumer {

    private final SwimmerService swimmerService;

    @RabbitListener(queues = "${spring.rabbitmq.queue.delete-coach-queue}")
    public void consumeDeleteCoachEvent(DeleteCoachEvent deleteCoachEvent) {
        log.info("CoachConsumer consumes DeleteCoachEvent with id: {}", deleteCoachEvent.coachId());
        int affectedSwimmers = swimmerService.unassignCoachFromSwimmers(deleteCoachEvent.coachId());
        log.info("{} swimmers were unassigned from coach {}", affectedSwimmers, deleteCoachEvent.coachId());
    }

    @RabbitListener(queues = "${spring.rabbitmq.queue.get-coach-swimmers-queue}")
    public List<Swimmer> consumeGetCoachSwimmersRequest(GetCoachSwimmersRequest getCoachSwimmersRequest, Message message) {
        String replyTo = message.getMessageProperties().getReplyTo();
        String correlationId = message.getMessageProperties().getCorrelationId();
        log.info("CoachConsumer consumes GetCoachSwimmersRequest with id: {}", getCoachSwimmersRequest.coachId());
        log.info("CoachConsumer is sending back the response to {} with correlation id {}", replyTo, correlationId);
        return swimmerService.findByCoachId(getCoachSwimmersRequest.coachId());
    }
}
