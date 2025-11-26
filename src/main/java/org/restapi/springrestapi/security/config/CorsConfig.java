package org.restapi.springrestapi.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


@Configuration
public class CorsConfig {

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