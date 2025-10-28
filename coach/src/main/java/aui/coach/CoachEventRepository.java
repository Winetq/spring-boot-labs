package aui.coach;

import aui.config.AccessTokenRetriever;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;

@Repository
public class CoachEventRepository {

    private final RestTemplate restTemplate;
    private final AccessTokenRetriever accessTokenRetriever;

    @Autowired
    public CoachEventRepository(@Value("${swimmer.service.url}") String baseUrl, AccessTokenRetriever accessTokenRetriever) {
        this.restTemplate = new RestTemplateBuilder().rootUri(baseUrl).build();
        this.accessTokenRetriever = accessTokenRetriever;
    }

    public ResponseEntity<String> delete(Coach coach) {
        HttpHeaders authHeader = createAuthHeader();
        HttpEntity<Void> request = new HttpEntity<>(authHeader);
        return restTemplate.exchange("/coaches/{id}", DELETE, request, String.class, coach.getId());
    }

    public ResponseEntity<String> getCoachSwimmers(Long id) {
        HttpHeaders authHeader = createAuthHeader();
        HttpEntity<Void> request = new HttpEntity<>(authHeader);
        return restTemplate.exchange("/coaches/" + id + "/swimmers", GET, request, String.class);
    }

    private HttpHeaders createAuthHeader() {
        String accessToken = accessTokenRetriever.retrieve();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return headers;
    }
}
