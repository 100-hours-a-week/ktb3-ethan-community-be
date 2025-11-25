package org.restapi.springrestapi.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        Token access,
        Token refresh
) {
    public record Token(
            String secret,
            Duration ttl
    ) {}
}