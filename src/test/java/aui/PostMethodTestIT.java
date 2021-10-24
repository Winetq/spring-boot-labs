package aui;

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

@SpringBootTest(classes = SpringBootLabsApplication.class)
@Test
public class PostMethodTestIT extends AbstractTestNGSpringContextTests { // e2e tests
    @Autowired
    private WebApplicationContext wac;

    private MockMvc mvc;

    @BeforeClass
    public void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test(dataProvider= "provideUriAndResponse")
    public void testCreateSwimmerAndCoach(String uri, String response, HttpStatus status) throws Exception {
        // given
        RequestBuilder request = MockMvcRequestBuilders.post(uri);
        SoftAssert sa = new SoftAssert();
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
                {"/swimmers/Michael/FREESTYLE", "A swimmer was added to the database!", HttpStatus.OK},
                {"/swimmers/Michael/FREESTYLE", "This swimmer was already created!", HttpStatus.CREATED},
                {"/swimmers/Jacob/BUTTERFLY", "A swimmer was added to the database!", HttpStatus.OK},
                {"/coaches/Jacek/5", "A coach was added to the database!", HttpStatus.OK},
                {"/coaches/Jacek/5", "This coach was already created!", HttpStatus.CREATED},
                {"/coaches/Rick/10", "A coach was added to the database!", HttpStatus.OK},
                {"/swimmers/qqq/1/BREASTSTROKE", "A swimmer was added to the database!", HttpStatus.OK},
                {"/swimmers/qqq/3/BACKSTROKE", "This coach does not exist!", HttpStatus.NOT_FOUND},
                {"/swimmers/qqq/2/BREASTSTROKE", "This swimmer was already created!", HttpStatus.CREATED}
        };
    }
}

