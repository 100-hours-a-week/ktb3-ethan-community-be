package org.restapi.springrestapi.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {
	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("Spring Rest API Document")
				.description("Spring REST API")
				.version("1.0.0")
			)
			.servers(List.of(
				new Server().url("http://localhost:8080").description("개발 서버")
			))
			.components(new Components()
				.addSecuritySchemes("Authorization",
					new SecurityScheme()
						.name("Authorization")
						.type(SecurityScheme.Type.APIKEY)
						.in(SecurityScheme.In.HEADER)
						.description("사용자 엑세스 토큰 주입 헤더(Bearer {access_token})"))
			)
			.addSecurityItem(new SecurityRequirement().addList(
				"Authorization"
			));
	}
}
