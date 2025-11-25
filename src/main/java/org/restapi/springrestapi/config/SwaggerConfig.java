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
				.addSecuritySchemes("access_token",
					new SecurityScheme()
						.name("access_token")
						.type(SecurityScheme.Type.APIKEY)
						.in(SecurityScheme.In.HEADER)
						.description("개발용 인증 토큰: 사용자 Public ID 주입 헤더"))
			)
			.addSecurityItem(new SecurityRequirement().addList(
				"access_token"
			));
	}
}
