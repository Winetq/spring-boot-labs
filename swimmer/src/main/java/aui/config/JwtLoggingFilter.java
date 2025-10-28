package aui.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static java.time.Duration.between;
import static java.time.Instant.now;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Order(HIGHEST_PRECEDENCE) // run as early as possible
@Component
@RequiredArgsConstructor
public class JwtLoggingFilter implements Filter {

    private static final String ROLE_CLAIM = "role";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtDecoder jwtDecoder;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String authHeader = httpRequest.getHeader(AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("Received request on {} - Access token present: NO", httpRequest.getRequestURI());
            chain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(BEARER_PREFIX.length());
            decodeToken(token, httpRequest);
        } catch (JwtException e) {
            log.error("Access token decode failed: {}", e.getMessage());
        }

        chain.doFilter(request, response);
    }

    private void decodeToken(String token, HttpServletRequest request) {
        Jwt jwt = jwtDecoder.decode(token);
        String subject = jwt.getSubject();
        String role = jwt.getClaimAsString(ROLE_CLAIM);
        long minutesUntilExpiry = between(now(), jwt.getExpiresAt()).toMinutes();
        log.info(
                "Received request on {} with access token - subject: {} ({}), expires in: {} minutes",
                request.getRequestURI(), subject, role, minutesUntilExpiry
        );
    }
}
