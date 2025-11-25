package org.restapi.springrestapi.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class JwtProvider {
    // Header
    private static final String ACCESS_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    // Cookie
    public static final String REFRESH_COOKIE = "refresh_token";

    // Claim
    private static final String CLAIM_TOKEN_TYPE = "typ";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    // Path
    public static final String REFRESH_PATH = "/auth/refresh";
    /*
        문자열 상수 너무 많은가? 별도의 중첩 클래스로 분류해야하는게 좋을까
     */

    private final JwtProperties props;

    private SecretKey accessKey;
    private SecretKey refreshKey;

    public JwtProvider(JwtProperties props) {
        this.props = props;
    }

    @PostConstruct
    void init() {
        this.accessKey = Keys.hmacShaKeyFor(
                props.access().secret().getBytes(StandardCharsets.UTF_8)
        );
        this.refreshKey = Keys.hmacShaKeyFor(
                props.refresh().secret().getBytes(StandardCharsets.UTF_8)
        );
    }

    /* =========================
       Token 생성
       ========================= */

    public String createAccessToken(Long userId) {
        return _createToken(userId, TYPE_ACCESS, accessKey, props.access().ttl());
    }

    public String createRefreshToken(Long userId) {
        return _createToken(userId, TYPE_REFRESH, refreshKey, props.refresh().ttl());
    }

    public ResponseCookie createRefreshCookie(String refreshToken) {
        return ResponseCookie.from(REFRESH_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path(REFRESH_PATH)
                .maxAge(props.refresh().ttl())
                .build();
    }

    private String _createToken(Long userId, String type, SecretKey key, Duration ttl) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + ttl.toMillis());

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(exp)
                .id(UUID.randomUUID().toString())
                .claim(CLAIM_TOKEN_TYPE, type)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /* =========================
       Token 파싱/검증
       ========================= */

    public boolean validateAccessToken(String token) {
        return _validate(token, accessKey, TYPE_ACCESS);
    }

    public boolean validateRefreshToken(String token) {
        return _validate(token, refreshKey, TYPE_REFRESH);
    }

    private boolean _validate(String token, SecretKey key, String expectedTyp) {
        try {
            Claims claims = _parse(token, key);
            String actualTyp = claims.get(CLAIM_TOKEN_TYPE, String.class);
            return expectedTyp.equals(actualTyp);
        } catch (JwtException | IllegalArgumentException e) {
            /*
            구체적인 실패 사유(서명 불일치, 토큰 형식, 만료 등)를 반환하면, 공격자도 알게된다.
            외부 응답은 단순하게, 내부 로그는 남기는 방향으로 가는게 좋아보인다.
             */
            log.error("토큰 유효성 검증 실패: " + e.getMessage());
            return false;
        }
    }


    public Authentication getAuthentication(String accessToken) {
        Long userId = getUserIdFromAccess(accessToken);
        return new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
    }

    /*
    - getUserIdFromAccess
    - getUserIdFromRefresh
    유효한 토큰에 대한 인증 정보 추출 메서드 이므로,
    토큰이 유효하지 않을 경우 예외 처리가 필요함. 추후 추가 예정
     */

    public Long getUserIdFromAccess(String token) {
        return Long.valueOf(_parse(token, accessKey).getSubject());
    }

    public Long getUserIdFromRefresh(String token) {
        return Long.valueOf(_parse(token, refreshKey).getSubject());
    }

    private Claims _parse(String token, SecretKey key) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /* =========================
       Request에서 토큰 꺼내기
       ========================= */

    public Optional<String> resolveAccessToken(HttpServletRequest request) {
        String header = request.getHeader(ACCESS_HEADER);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return Optional.of(header.substring(BEARER_PREFIX.length()));
        }
        return Optional.empty();
    }

    public Optional<String> resolveRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return Optional.empty();

        for (Cookie c : cookies) {
            if (REFRESH_COOKIE.equals(c.getName())) {
                return Optional.ofNullable(c.getValue());
            }
        }
        return Optional.empty();
    }
}