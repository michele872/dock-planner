package dev.magurno.dockplanner.common;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	OpenAPI dockPlannerOpenApi() {
		return new OpenAPI().info(new Info().title("Dock Planner API")
			.version("1.0")
			.description("Book warehouse loading docks for trucks: size checks, no double bookings, free slots."));
	}

}
