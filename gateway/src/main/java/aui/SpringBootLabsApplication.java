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

@SpringBootApplication
public class SpringBootLabsApplication {

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
		corsConfig.setAllowedOrigins(singletonList("http://localhost:8083"));
		corsConfig.setMaxAge(3600L);
		corsConfig.setAllowedMethods(asList("GET", "POST", "DELETE", "PUT", "OPTIONS"));
		corsConfig.addAllowedHeader("*");

		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", corsConfig);

		return new CorsWebFilter(source);
	}
}
