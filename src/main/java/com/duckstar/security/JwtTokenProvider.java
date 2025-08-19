package com.duckstar.security;

import com.duckstar.security.domain.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String secretKeyRaw;

    private Key secretKey;

    private final long accessTokenValidTime = 1000L * 60 * 60; // 1시간
    private final long refreshTokenValidTime = 1000L * 60 * 60 * 24 * 7; // 7일

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyRaw.getBytes());
    }

    public String createAccessToken(Long memberId, Role role) {
        return createToken(memberId, role, accessTokenValidTime);
    }

    public String createRefreshToken(Long memberId, Role role) {
        return createToken(memberId, role, refreshTokenValidTime);
    }

    private String createToken(Long memberId, Role role, long validTime) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validTime);

        return Jwts.builder()
                .setSubject(String.valueOf(memberId))
                .claim("role", role.name())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateClaims(Claims claims) {
        return claims != null && !claims.getExpiration().before(new Date());
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
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
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
}
