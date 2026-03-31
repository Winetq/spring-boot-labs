package aui;

import aui.configuration.IntegrationTestConfiguration;
import aui.rabbitmq.GetCoachSwimmersRequest;
import aui.swimmer.Swimmer;
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

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public class PostMethodTestIT extends IntegrationTestConfiguration {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mvc;

    @Value("${spring.rabbitmq.queue.get-coach-swimmers-queue}")
    private String getCoachSwimmersQueue;

    @BeforeClass
    public void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test(dataProvider = "provideUriAndResponse")
    public void testCreateSwimmerWithCoach(String uri, String json, String response, HttpStatus status) throws Exception {
        // given
        RequestBuilder request = MockMvcRequestBuilders
                .post(uri)
                .contentType(APPLICATION_JSON)
                .content(json);

        // when
        MvcResult result = mvc.perform(request).andReturn();

        // then
        SoftAssert sa = new SoftAssert();
        sa.assertEquals(result.getResponse().getContentAsString(), response);
        sa.assertEquals(result.getResponse().getStatus(), status.value());
        sa.assertAll();
    }

    @Test(dependsOnMethods = "testCreateSwimmerWithCoach")
    public void shouldReturnSwimmersOfCoach4() {
        // given
        long coachId = 4L;

        // when
        List<Swimmer> newSwimmers = rabbitTemplate.convertSendAndReceiveAsType(
                getCoachSwimmersQueue,
                new GetCoachSwimmersRequest(coachId),
                new ParameterizedTypeReference<>() {}
        );

        // then
        SoftAssert sa = new SoftAssert();
        sa.assertEquals(newSwimmers.size(), 4);
        sa.assertTrue(newSwimmers.stream().allMatch(swimmer -> swimmer.getCoachId() == coachId));
        sa.assertAll();
    }

    @DataProvider
    public Object[][] provideUriAndResponse() {
        return new Object[][] {
                {"/swimmers", "{ \"name\": \"Michael\", \"coachId\": \"4\", \"specialization\": \"BUTTERFLY\" }",
                        "Swimmer Michael was added to the database!", CREATED},
                {"/swimmers", "{ \"name\": \"Alfred\", \"coachId\": \"4\", \"specialization\": \"FREESTYLE\" }",
                        "Swimmer Alfred was added to the database!", CREATED},
                {"/swimmers", "{ \"name\": \"Jacob\", \"coachId\": \"4\", \"specialization\": \"BACKSTROKE\" }",
                        "Swimmer Jacob was added to the database!", CREATED},
                {"/swimmers", "{ \"name\": \"Stephan\", \"coachId\": \"4\", \"specialization\": \"BREASTSTROKE\" }",
                        "Swimmer Stephan was added to the database!", CREATED}
        };
    }
}
