package org.restapi.springrestapi.config;

import org.springframework.stereotype.Component;

@Component
public class DummyPasswordEncoder implements PasswordEncoder {
	public String encode(String rawPassword) {
		return "dummyEncodedPassword{"+rawPassword+"}";
	}
	public boolean matches(String rawPassword, String encodedPassword) {
		return this.encode(rawPassword).equals(encodedPassword);
	}
}
