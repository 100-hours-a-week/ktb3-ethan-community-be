package org.restapi.springrestapi.config;

public interface PasswordEncoder {
	String encode(String rawPassword);
	boolean matches(String rawPassword, String encodedPassword);
}
