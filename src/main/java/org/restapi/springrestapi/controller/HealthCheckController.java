package org.restapi.springrestapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Health", description = "헬스체크 API")
public class HealthCheckController {
	@Operation(summary = "헬스 체크", description = "애플리케이션의 상태를 확인합니다.")
	@GetMapping("/hc")
	public ResponseEntity<?> getStatus() {
		return ResponseEntity.ok()
			.body("hello");
	}

	@GetMapping("/csrf")
	public ResponseEntity<Void> csrf() {
		return ResponseEntity.ok().build();
	}
}
