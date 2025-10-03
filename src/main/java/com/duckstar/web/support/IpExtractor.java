package com.duckstar.web.support;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class IpExtractor {
    public String extract(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            // 여러 IP가 있을 수 있어서 첫 번째만 사용
            return ip.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
