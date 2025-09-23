package com.duckstar.security.presentation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SimpleRateLimiter {

    private final Map<String, List<LocalDateTime>> requests = new ConcurrentHashMap<>();
    private final int maxRequests = 10; // 1분당 최대 10회
    private final Duration window = Duration.ofMinutes(1); // 1분 윈도우

    public boolean isAllowed(String key) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.minus(window);

        List<LocalDateTime> userRequests = requests.computeIfAbsent(key, k -> new ArrayList<>());

        // 오래된 요청 제거
        userRequests.removeIf(time -> time.isBefore(cutoff));

        if (userRequests.size() >= maxRequests) {
            log.warn("Rate limit exceeded for key: {}, current requests: {}", key, userRequests.size());
            return false;
        }

        userRequests.add(now);
        log.debug("Rate limit check passed for key: {}, current requests: {}", key, userRequests.size());
        return true;
    }

    // IP별 제한
    public boolean isAllowedByIp(String ip) {
        return isAllowed("ip:" + ip);
    }

    // 사용자별 제한
    public boolean isAllowedByUser(Long userId) {
        return isAllowed("user:" + userId);
    }
}
