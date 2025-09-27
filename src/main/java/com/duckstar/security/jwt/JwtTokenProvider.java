package com.duckstar.security.jwt;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.AuthHandler;
import com.duckstar.security.domain.enums.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String secretKeyRaw;

    private Key secretKey;

    @Value("${jwt.issuer}")
    private String issuer;

    private final long accessTokenValidTime = 1000L * 60 * 60; // 1시간
    private final long refreshTokenValidTime = 1000L * 60 * 60 * 24 * 7; // 7일

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyRaw.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long memberId, Role role) {
        return createToken(memberId, role, accessTokenValidTime, "ACCESS");
    }

    public String createRefreshToken(Long memberId, Role role) {
        return createToken(memberId, role, refreshTokenValidTime, "REFRESH");
    }

    private String createToken(Long memberId, Role role, long validTime, String type) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validTime);

        return Jwts.builder()
                .setSubject(String.valueOf(memberId))
                .claim("role", role.name())
                .setIssuer(issuer)
                .claim("typ", type)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        Claims claims = parseClaims(token);
        return validateClaims(claims);
    }

    public Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("JWT 만료: {}", e.getMessage());
            throw new AuthHandler(ErrorStatus.ACCESS_TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT 파싱 실패: {}", e.getMessage());
            throw new AuthHandler(ErrorStatus.INVALID_TOKEN);
        }
    }

    private boolean validateClaims(Claims claims) {
        return !claims.getExpiration().before(new Date()) &&
                issuer.equals(claims.getIssuer());
    }

    public boolean isAccessToken(Claims claims) {
        return "ACCESS".equals(claims.get("typ", String.class));
    }

    public boolean isRefreshToken(Claims claims) {
        return "REFRESH".equals(claims.get("typ", String.class));
    }

    public String resolveFromCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public Long extractMemberId(String token) {
        try {
            return Long.valueOf(Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject());
        } catch (JwtException e) {
            throw new AuthHandler(ErrorStatus.INVALID_TOKEN);
        }
    }
}
