package org.restapi.springrestapi.service.post;

import jakarta.servlet.http.HttpServletRequest;
import org.restapi.springrestapi.service.auth.UserKeyGenerator;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LocalPostViewDebounce {
    private final Map<String, Long> seen = new ConcurrentHashMap<>();

    public boolean seenRecently(HttpServletRequest request, Long userId, Long postId) {
        String userKey = UserKeyGenerator.generate(request, userId);
        String key = "post:view:seen:" + postId + ":" + userKey;
        Long now = Instant.now().toEpochMilli();

        Long lastSeen = seen.get(key);
        final long refreshInterval = 10 * 1000; // 10초 이내 재방문시 카운트 하지 않음
        if (lastSeen != null && now - lastSeen < refreshInterval) {
            return true;
        }
        seen.put(key, now);
        return false;
    }
}
