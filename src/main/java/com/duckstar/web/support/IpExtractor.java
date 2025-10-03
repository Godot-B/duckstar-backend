package com.duckstar.web.support;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class IpExtractor {
    public String extract(HttpServletRequest request) {
        // X-Forwarded-For 헤더 확인 (로드밸런서, 프록시에서 사용)
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            // 여러 IP가 있을 수 있어서 첫 번째만 사용
            return xForwardedFor.split(",")[0].trim();
        }
        
        // X-Real-IP 헤더 확인 (Nginx에서 사용)
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        
        // CF-Connecting-IP 헤더 확인 (Cloudflare)
        String cfConnectingIp = request.getHeader("CF-Connecting-IP");
        if (cfConnectingIp != null && !cfConnectingIp.isBlank()) {
            return cfConnectingIp.trim();
        }
        
        // 기본값: 직접 연결된 클라이언트 IP
        return request.getRemoteAddr();
    }
}
