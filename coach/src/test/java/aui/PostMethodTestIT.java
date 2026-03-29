package aui;

import aui.coach.Coach;
import aui.coach.CoachService;
import aui.configuration.IntegrationTestConfiguration;
import aui.rabbitmq.DeleteCoachEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

public class PostMethodTestIT extends IntegrationTestConfiguration {

    @Autowired
    private CoachService coachService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mvc;

    @Value("${spring.rabbitmq.queue.delete-coach-queue}")
    private String deleteCoachQueue;

    @BeforeClass
    public void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
    }

    @Test(dataProvider = "provideUriAndResponse")
    public void testCreateCoach(String uri, String json, String response, HttpStatus status) throws Exception {
        // given
        RequestBuilder request = MockMvcRequestBuilders
                .post(uri)
                .contentType(APPLICATION_JSON)
                .content(json)
                .with(jwt().authorities(() -> "user"));

        // when
        MvcResult result = mvc.perform(request).andReturn();

        // then
        SoftAssert sa = new SoftAssert();
        sa.assertEquals(result.getResponse().getContentAsString(), response);
        sa.assertEquals(result.getResponse().getStatus(), status.value());
        sa.assertAll();
    }

    @Test(dependsOnMethods = "testCreateCoach")
    public void testDeleteCoach() throws Exception {
        // given
        long coachId = 2L;
        RequestBuilder request = MockMvcRequestBuilders
                .delete("/coaches/" + coachId)
                .with(jwt().authorities(() -> "admin"));

        // when
        MvcResult result = mvc.perform(request).andReturn();

        // then
        List<Coach> all = coachService.findAll();
        DeleteCoachEvent event = rabbitTemplate.receiveAndConvert(deleteCoachQueue, new ParameterizedTypeReference<>() {});

        SoftAssert sa = new SoftAssert();
        sa.assertEquals(result.getResponse().getStatus(), OK.value());
        sa.assertEquals(all.size(), 2);
        sa.assertEquals(all.stream().map(Coach::getName).collect(toSet()), Set.of("Alfons", "Ben"));
        sa.assertNotNull(event, "Expected a DeleteCoachEvent in the queue");
        sa.assertEquals(event.coachId(), Long.valueOf(2L));
        sa.assertAll();
    }

    @DataProvider
    public Object[][] provideUriAndResponse() {
        return new Object[][] {
                {"/coaches", "{ \"name\": \"Alfons\", \"level\": 10 }",
                        "Coach Alfons was added to the database!", CREATED},
                {"/coaches", "{ \"name\": \"Andrew\", \"level\": 12 }",
                        "Coach Andrew was added to the database!", CREATED},
                {"/coaches", "{ \"name\": \"Ben\", \"level\": 5 }",
                        "Coach Ben was added to the database!", CREATED}
        };
    }
}
