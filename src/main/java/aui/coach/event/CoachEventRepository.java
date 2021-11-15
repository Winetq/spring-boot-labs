package aui.coach.event;

import aui.coach.Coach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

@Repository
public class CoachEventRepository {
    private final RestTemplate restTemplate;

    @Autowired
    public CoachEventRepository(@Value("${lab3.swimmer.url}") String baseUrl) {
        restTemplate = new RestTemplateBuilder().rootUri(baseUrl).build();
    }

    public void create(Coach coach) {
        restTemplate.postForLocation("/coaches", CreateCoachRequest.entityToDto(coach)); // event request
    }

    public void delete(Coach coach) {
        restTemplate.delete("/coaches/{id}", coach.getId()); // event request
    }

    public ResponseEntity<String> getCoachSwimmers(Long id) {
        return restTemplate.getForEntity("/coaches/" + id + "/swimmers", String.class);
    }
}

