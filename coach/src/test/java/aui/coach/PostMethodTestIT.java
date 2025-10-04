package aui.coach;

import aui.SpringBootLabsApplication;
import aui.coach.event.CoachEventRepository;
import io.restassured.module.mockmvc.response.MockMvcResponse;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = SpringBootLabsApplication.class)
@Test
public class PostMethodTestIT {
    private CoachEventRepository eventRepository = mock(CoachEventRepository.class); // this class will be mocked
    private CoachRepository coachRepository = spy(CoachRepository.class); // this class WON'T be mocked

    @BeforeClass
    public void setup() {
        doNothing().when(eventRepository).create(any(Coach.class));
    }

    @Test(dataProvider= "provideUriAndResponse")
    public void testCreateCoach(String uri, String json, String response, HttpStatus status) {
        // given
        SoftAssert sa = new SoftAssert();
        // when
        MockMvcResponse result = given()
                .standaloneSetup(new CoachController(new CoachService(coachRepository, eventRepository)))
                .header("Content-Type", "application/json")
                .body(json)
                .when()
                .post(uri);
        // then
        sa.assertEquals(result.getBody().asString(), response);
        sa.assertEquals(result.statusCode(), status.value());
        sa.assertAll();
    }

    @DataProvider
    public Object[][] provideUriAndResponse() {
        return new Object[][] {
                {"/coaches", "{ \"name\": \"Alfons\", \"level\": 10 }",
                        "A coach was added to the database!", HttpStatus.OK},
                // {"/coaches", "{ \"name\": \"Alfons\", \"level\": 10 }",
                        // "This coach was already created!", HttpStatus.BAD_REQUEST},
                {"/coaches", "{ \"name\": \"Alfonso\", \"level\": 5 }",
                        "A coach was added to the database!", HttpStatus.OK}
        };
    }
}

