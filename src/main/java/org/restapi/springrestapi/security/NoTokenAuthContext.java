package org.restapi.springrestapi.security;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NoTokenAuthContext implements AuthContext {
	private final HttpServletRequest request;

	@Override
	public Long currentUserIdOrNull() {
		String userId = request.getHeader("access_token");
		if (userId == null || userId.isBlank()) {
			return null;
		}
		try {
			return Long.parseLong(userId);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}