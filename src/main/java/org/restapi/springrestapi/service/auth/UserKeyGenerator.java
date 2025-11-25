package org.restapi.springrestapi.service.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

public class UserKeyGenerator {
    public static String generate(HttpServletRequest request, Long userId) {
        if (userId != null) {
            return "user:" + userId;
        }
        String ip = getRealClientIp(request);
        String ua = request.getHeader("User-Agent");
        String raw = (ip == null ? "" : ip) + "|" + (ua == null ? "" : ua);

        return "anon:" + DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
    }

    private static String getRealClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");

        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }

        return req.getRemoteAddr();
    }
}
