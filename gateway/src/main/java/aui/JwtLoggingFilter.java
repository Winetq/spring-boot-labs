package aui;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.*;

@Slf4j
@Order(-100) // run before security filters
@Component
public class JwtLoggingFilter implements WebFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        HttpMethod httpMethod = request.getMethod();

        if (httpMethod != null && httpMethod.matches(OPTIONS.name())) { // skip CORS preflight requests
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(AUTHORIZATION);

        if (authHeader != null) {
            String token = authHeader.substring(BEARER_PREFIX.length());
            log.info("Request to {} {} - Access token present: YES, Token: {}", httpMethod, path, token.substring(0, Math.min(20, token.length())) + "...");
        } else {
            log.warn("Request to {} {} - Access token present: NO", httpMethod, path);
        }

        return chain.filter(exchange);
    }
}
