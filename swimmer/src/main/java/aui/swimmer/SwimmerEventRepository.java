package aui.swimmer;

import aui.config.AccessTokenRetriever;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Repository
public class SwimmerEventRepository {

    private final RestTemplate restTemplate;
    private final AccessTokenRetriever accessTokenRetriever;

    @Autowired
    public SwimmerEventRepository(@Value("${lab3.coach.url}") String baseUrl, AccessTokenRetriever accessTokenRetriever) {
        this.restTemplate = new RestTemplateBuilder().rootUri(baseUrl).build();
        this.accessTokenRetriever = accessTokenRetriever;
    }

    public ResponseEntity<String> getSwimmerCoach(Swimmer swimmer) {
        if (swimmer.getCoach() == null) {
            return new ResponseEntity<>(NOT_FOUND);
        } else {
            HttpHeaders authHeader = createAuthHeader();
            HttpEntity<Void> request = new HttpEntity<>(authHeader);
            return restTemplate.exchange("/coaches/" + swimmer.getCoach().getId(), GET, request, String.class);
        }
    }

    private HttpHeaders createAuthHeader() {
        String accessToken = accessTokenRetriever.retrieve();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return headers;
    }
}
