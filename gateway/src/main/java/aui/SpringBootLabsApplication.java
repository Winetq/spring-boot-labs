package aui;

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
    private static final String CLIENT_HOST = "http://localhost:8083";

    public static void main(String[] args) {
		SpringApplication.run(SpringBootLabsApplication.class, args);
	}

	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
		return builder
				.routes()
				.route("coaches", r -> r
						.host("localhost:8080")
						.and()
						.path("/coaches/**", "/coaches")
						.uri("http://coaches:8081"))
				.route("swimmers", r -> r
						.host("localhost:8080")
						.and()
						.path("/swimmers/**", "/swimmers")
						.uri("http://swimmers:8082"))
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
