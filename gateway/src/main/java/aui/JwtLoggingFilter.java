package aui;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import static java.time.Duration.between;
import static java.time.Instant.now;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.OPTIONS;
import static reactor.core.publisher.Mono.empty;

@Slf4j
@Order(-100) // run before security filters
@Component
@RequiredArgsConstructor
public class JwtLoggingFilter implements WebFilter {

    private static final String ROLE_CLAIM = "role";
    private static final String BEARER_PREFIX = "Bearer ";

    private final ReactiveJwtDecoder jwtDecoder;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        HttpMethod httpMethod = request.getMethod();

        if (httpMethod != null && httpMethod.matches(OPTIONS.name())) { // skip CORS preflight requests
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length());
            return decodeToken(token, exchange, chain, httpMethod, path);
        }

        log.warn("Request to {} {} - Access token present: NO", httpMethod, path);
        return chain.filter(exchange);
    }

    private Mono<Void> decodeToken(String token, ServerWebExchange exchange, WebFilterChain chain, HttpMethod httpMethod, String path) {
        return jwtDecoder.decode(token)
                .doOnNext(jwt -> {
                    String subject = jwt.getSubject();
                    String role = jwt.getClaimAsString(ROLE_CLAIM);
                    long minutesUntilExpiry = between(now(), jwt.getExpiresAt()).toMinutes();
                    log.info(
                            "Request to {} {} - Access token decoded - Subject: {} ({}), Expires in: {} minutes",
                            httpMethod, path, subject, role, minutesUntilExpiry
                    );
                })
                .onErrorResume(e -> {
                    log.error("Request to {} {} - Access token decode failed: {}", httpMethod, path, e.getMessage());
                    return empty();
                })
                .then(chain.filter(exchange));
    }
}
