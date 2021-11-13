//package aui;
//
//import aui.coach.Coach;
//import aui.coach.CoachController;
//import aui.coach.CoachRepository;
//import aui.coach.CoachService;
//import aui.coach.dto.GETCoachDTO;
//import aui.swimmer.*;
//import aui.swimmer.dto.GETSwimmerDTO;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import io.restassured.module.mockmvc.response.MockMvcResponse;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.MockitoAnnotations;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.HttpStatus;
//import org.testng.annotations.BeforeTest;
//import org.testng.annotations.Test;
//import org.testng.asserts.SoftAssert;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
//import static org.mockito.ArgumentMatchers.anyLong;
//
//@SpringBootTest(classes = SpringBootLabsApplication.class)
//@Test
//public class GetMethodTestIT {
//    private final Swimmer swimmer1 = new Swimmer("swimmer1", SwimmingStyle.FREESTYLE);
//    private final Swimmer swimmer2 = new Swimmer("swimmer2", SwimmingStyle.BUTTERFLY);
//    private final Swimmer swimmer3 = new Swimmer("swimmer3", SwimmingStyle.BACKSTROKE);
//    private final Swimmer swimmer4 = new Swimmer("swimmer4", SwimmingStyle.BREASTSTROKE);
//    private final Coach coach1 = new Coach("coach1", new ArrayList<>(), 5);
//    private final Coach coach2 = new Coach("coach2", new ArrayList<>(), 10);
//    private final List<Swimmer> swimmers = List.of(swimmer1, swimmer2, swimmer3, swimmer4);
//    private final List<Coach> coaches = List.of(coach1, coach2);
//
//    @Mock
//    private SwimmerRepository swimmerRepository;
//    @InjectMocks
//    private SwimmerService swimmerService;
//
//    @Mock
//    private CoachRepository coachRepository;
//    @InjectMocks
//    private CoachService coachService;
//
//    @BeforeTest
//    public void setup() {
//        MockitoAnnotations.openMocks(this);
//        swimmer3.assignCoach(coach1);
//        coach1.addSwimmer(swimmer3);
//        swimmer4.assignCoach(coach2);
//        coach2.addSwimmer(swimmer4);
//        Mockito.when(swimmerRepository.findAll()).thenReturn(swimmers);
//        Mockito.when(coachRepository.findAll()).thenReturn(coaches);
//        Mockito.when(swimmerRepository.findById(anyLong())).thenReturn(Optional.of(swimmer2));
//        Mockito.when(coachRepository.findById(anyLong())).thenReturn(Optional.of(coach2));
//    }
//
//    public void testGetSwimmers() throws JsonProcessingException {
//        // given
//        String uri = "swimmers";
//        ObjectMapper jacksonMapper = new ObjectMapper();
//        SoftAssert sa = new SoftAssert();
//        // when
//        MockMvcResponse response = given()
//                .standaloneSetup(new SwimmerController(swimmerService, coachService))
//                .when()
//                .get(uri);
//        // then
//        sa.assertEquals(response.getBody().asString(), jacksonMapper.writeValueAsString(GETSwimmerDTO.entityToDTO(swimmers)));
//        sa.assertEquals(response.statusCode(), HttpStatus.OK.value());
//        sa.assertAll();
//    }
//
//    public void testGetCoaches() throws JsonProcessingException {
//        // given
//        String uri = "coaches";
//        ObjectMapper jacksonMapper = new ObjectMapper();
//        SoftAssert sa = new SoftAssert();
//        // when
//        MockMvcResponse response = given()
//                .standaloneSetup(new CoachController(coachService, swimmerService))
//                .when()
//                .get(uri);
//        // then
//        sa.assertEquals(response.getBody().asString(), jacksonMapper.writeValueAsString(GETCoachDTO.entityToDTO(coaches)));
//        sa.assertEquals(response.statusCode(), HttpStatus.OK.value());
//        sa.assertAll();
//    }
//
//    public void testGetSwimmerById() throws JsonProcessingException {
//        // given
//        String uri = "swimmers/" + anyLong();
//        ObjectMapper jacksonMapper = new ObjectMapper();
//        SoftAssert sa = new SoftAssert();
//        // when
//        MockMvcResponse response = given()
//                .standaloneSetup(new SwimmerController(swimmerService, coachService))
//                .when()
//                .get(uri);
//        // then
//        sa.assertEquals(response.getBody().asString(), jacksonMapper.writeValueAsString(GETSwimmerDTO.entityToDTO(swimmer2)));
//        sa.assertEquals(response.statusCode(), HttpStatus.OK.value());
//        sa.assertAll();
//    }
//
//    public void testGetCoachById() throws JsonProcessingException {
//        // given
//        String uri = "coaches/" + anyLong();
//        ObjectMapper jacksonMapper = new ObjectMapper();
//        SoftAssert sa = new SoftAssert();
//        // when
//        MockMvcResponse response = given()
//                .standaloneSetup(new CoachController(coachService, swimmerService))
//                .when()
//                .get(uri);
//        // then
//        sa.assertEquals(response.getBody().asString(), jacksonMapper.writeValueAsString(GETCoachDTO.entityToDTO(coach2)));
//        sa.assertEquals(response.statusCode(), HttpStatus.OK.value());
//        sa.assertAll();
//    }
//}
//
