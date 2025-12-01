package org.restapi.springrestapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;

@RestController
public class HealthCheckController {
	@Operation(summary = "헬스 체크", description = "애플리케이션의 상태를 확인합니다.")
	@GetMapping("/hc")
	public ResponseEntity<?> getStatus() {
		return ResponseEntity.ok()
			.body("hello");
	}

    @Operation(summary = "CSRF 토큰 요청", description = "CSRF 토큰을 요청합니다.")
	@GetMapping("/csrf")
	public ResponseEntity<CsrfToken> csrf(CsrfToken csrfToken) {
		return ResponseEntity.ok(csrfToken);
	}
}
