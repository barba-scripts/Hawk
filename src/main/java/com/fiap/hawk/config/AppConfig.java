package com.fiap.hawk.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, SeedProperties.class})
public class AppConfig {

	@Bean
	OpenAPI hawkOpenApi() {
		final String scheme = "bearerAuth";
		return new OpenAPI()
				.info(new Info()
						.title("Hawk API")
						.version("v1")
						.description("API da plataforma de inovação Hawk — Grupo Águia Branca"))
				.addSecurityItem(new SecurityRequirement().addList(scheme))
				.components(new Components().addSecuritySchemes(scheme,
						new SecurityScheme()
								.name(scheme)
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")));
	}
}
