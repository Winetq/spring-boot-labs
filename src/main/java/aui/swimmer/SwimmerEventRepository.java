package aui.swimmer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

@Repository
public class SwimmerEventRepository {
    private final RestTemplate restTemplate;

    @Autowired
    public SwimmerEventRepository(@Value("${lab3.coach.url}") String baseUrl) {
        restTemplate = new RestTemplateBuilder().rootUri(baseUrl).build();
    }

    public ResponseEntity<String> getSwimmerCoach(Swimmer swimmer) {
        return restTemplate.getForEntity("/coaches/" + swimmer.getCoach().getId(), String.class);
    }
}

