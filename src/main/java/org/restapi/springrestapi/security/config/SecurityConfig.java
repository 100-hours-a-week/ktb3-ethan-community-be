package org.restapi.springrestapi.security.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.restapi.springrestapi.security.handler.RestAccessDeniedHandler;
import org.restapi.springrestapi.security.handler.RestAuthenticationEntryPoint;
import org.restapi.springrestapi.security.jwt.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtFilter jwtFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		CsrfTokenRequestAttributeHandler requestHandler =
			new CsrfTokenRequestAttributeHandler();

        CookieCsrfTokenRepository cookieCsrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        cookieCsrfTokenRepository.setCookieCustomizer(builder -> builder
                .path("/")
                .sameSite("None")
                .secure(true)
        );

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .csrf(csrf -> csrf
                        .csrfTokenRepository(cookieCsrfTokenRepository)
                        .csrfTokenRequestHandler(requestHandler)
                        .requireCsrfProtectionMatcher(
                                new RegexRequestMatcher("/auth/refresh", "POST")
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/hc", "/csrf").permitAll()
                        .requestMatchers("/upload/**").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/auth/login",
                                "/auth/signup",
                                "/auth/refresh"
						).permitAll()
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/*", "/posts", "/posts/**", "/post", "/posts/*/comments").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(new RestAuthenticationEntryPoint(objectMapper))
                        .accessDeniedHandler(new RestAccessDeniedHandler(objectMapper))
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        ;
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 출처 설정(vscode live server 5500, vite react local server 5173)
        configuration.setAllowedOrigins(List.of("http://127.0.0.1:5500","http://localhost:5500", "http://localhost:5173"));

        // 허용할 HTTP 메서드 설정
        configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));

        // 허용할 HTTP 헤더 설정
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-XSRF-TOKEN"));

        // 브라우저에 노출할 헤더 설정
//        configuration.setExposedHeaders(List.of("Custom-Header"));

        // 자격 증명(쿠키, 인증 헤더 등)을 허용할지 여부
        configuration.setAllowCredentials(true);

        // 예비 요청(Preflight) 결과 캐시 시간 설정
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 모든 경로에 대해 위에서 정의한 CORS 정책 적용
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
