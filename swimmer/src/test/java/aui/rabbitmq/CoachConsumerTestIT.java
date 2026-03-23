package aui.rabbitmq;

import aui.configuration.IntegrationTestConfiguration;
import aui.swimmer.Swimmer;
import aui.swimmer.SwimmerService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

import org.testng.asserts.SoftAssert;

public class CoachConsumerTestIT extends IntegrationTestConfiguration {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private SwimmerService swimmerService;

    @Value("${spring.rabbitmq.queue.get-coach-swimmers-queue}")
    private String getCoachSwimmersQueue;

    @Value("${spring.rabbitmq.queue.delete-coach-queue}")
    private String deleteCoachQueue;

    @Test
    public void shouldReturnSwimmersWhenGetCoachSwimmersRequestConsumed() {
        // given - init.sql inserts swimmers with coachId=2
        long coachId = 2L;
        List<Swimmer> expectedSwimmers = swimmerService.findByCoachId(coachId);

        // when
        List<Swimmer> actualSwimmers = rabbitTemplate.convertSendAndReceiveAsType(
                getCoachSwimmersQueue,
                new GetCoachSwimmersRequest(coachId),
                new ParameterizedTypeReference<>() {
                }
        );

        // then
        SoftAssert sa = new SoftAssert();
        sa.assertEquals(actualSwimmers.size(), 3);
        sa.assertEquals(expectedSwimmers.size(), 3);
        sa.assertEquals(actualSwimmers, expectedSwimmers);
        sa.assertAll();
    }

    @Test
    public void shouldUnassignSwimmersWhenDeleteCoachEventConsumed() {
        // given - init.sql inserts swimmers with coachId=1
        long coachId = 1L;
        List<Swimmer> swimmersBefore = swimmerService.findByCoachId(coachId);

        // when
        rabbitTemplate.convertAndSend(deleteCoachQueue, new DeleteCoachEvent(coachId));

        // then
        await().atMost(5, SECONDS)
                .untilAsserted(() -> {
                    List<Swimmer> swimmersAfter = swimmerService.findByCoachId(coachId);

                    SoftAssert sa = new SoftAssert();
                    sa.assertEquals(swimmersBefore.size(), 4, "Swimmers before should be 4");
                    sa.assertEquals(swimmersAfter.size(), 0, "Swimmers after should be 0");
                    sa.assertAll();
                });
    }
}
