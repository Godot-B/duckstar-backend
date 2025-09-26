package com.duckstar.security.controller;

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

    public boolean isAllowed(String key, int maxRequests, Duration window) {
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

    // 편의 메서드
    // IP 별 제한
    public boolean isAllowedByIp(String ip, int maxRequests, Duration window) {
        return isAllowed("ip:" + ip, maxRequests, window);
    }

    // 사용자 별 제한
    public boolean isAllowedByUser(Long userId, int maxRequests, Duration window) {
        return isAllowed("user:" + userId, maxRequests, window);
    }
}
