package aui.coach;

import aui.SpringBootLabsApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
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

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(classes = SpringBootLabsApplication.class)
public class PostMethodTestIT extends AbstractTestNGSpringContextTests { // e2e tests

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mvc;

    @BeforeClass
    public void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test(dataProvider = "provideUriAndResponse")
    public void testCreateCoach(String uri, String json, String response, HttpStatus status) throws Exception {
        // given
        SoftAssert sa = new SoftAssert();
        RequestBuilder request = MockMvcRequestBuilders
                .post(uri)
                .contentType(APPLICATION_JSON)
                .content(json);

        // when
        MvcResult result = mvc.perform(request).andReturn();

        // then
        sa.assertEquals(result.getResponse().getContentAsString(), response);
        sa.assertEquals(result.getResponse().getStatus(), status.value());
        sa.assertAll();
    }

    @DataProvider
    public Object[][] provideUriAndResponse() {
        return new Object[][] {
                {"/coaches", "{ \"name\": \"Alfons\", \"level\": 10 }",
                        "Coach Alfons was added to the database!", CREATED},
                {"/coaches", "{ \"name\": \"Alfons\", \"level\": 12 }",
                        "Coach Alfons was added to the database!", CREATED},
                {"/coaches", "{ \"name\": \"Alfonso\", \"level\": 5 }",
                        "Coach Alfonso was added to the database!", CREATED}
        };
    }
}
