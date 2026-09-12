package aui;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

@SpringBootApplication
public class SpringBootLabsApplication {

    private static final String ALLOW_ALL = "*";

	@Value("${spring.gateway.host}")
    private String GATEWAY_HOST;

	@Value("${spring.coach.uri}")
	private String COACH_HOST;

	@Value("${spring.swimmer.uri}")
	private String SWIMMER_HOST;

	@Value("${spring.client.uri}")
	private String CLIENT_HOST;

    public static void main(String[] args) {
		SpringApplication.run(SpringBootLabsApplication.class, args);
	}

	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
		return builder
				.routes()
				.route("coach", r -> r
						.host(GATEWAY_HOST)
						.and()
						.path("/coaches/**", "/coaches")
						.uri(COACH_HOST))
				.route("swimmer", r -> r
						.host(GATEWAY_HOST)
						.and()
						.path("/swimmers/**", "/swimmers")
						.uri(SWIMMER_HOST))
				.build();
	}

	@Bean
	public CorsWebFilter corsWebFilter() {

		final CorsConfiguration corsConfig = new CorsConfiguration();
		corsConfig.setAllowedOrigins(singletonList(CLIENT_HOST));
		corsConfig.setMaxAge(3600L);
		corsConfig.setAllowedMethods(asList(GET.name(), POST.name(), DELETE.name(), PUT.name(), OPTIONS.name()));
		corsConfig.addAllowedHeader(ALLOW_ALL);

		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", corsConfig);

		return new CorsWebFilter(source);
	}
}
