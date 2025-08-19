package com.duckstar.security;

import com.duckstar.domain.Member;
import com.duckstar.security.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String jwtAccessToken = resolveBearer(req);
        if (jwtAccessToken == null) {
            jwtAccessToken = resolveFromCookie(req, "ACCESS_TOKEN");
        }

        Claims accessClaims = jwtTokenProvider.parseClaims(jwtAccessToken);
        if (jwtAccessToken != null && jwtTokenProvider.validateClaims(accessClaims)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Long memberId = Long.valueOf(accessClaims.getSubject());
                Member member = memberRepository.findById(memberId).orElse(null);
                if (member != null) {
                    MemberPrincipal principal = MemberPrincipal.of(member);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    principal,
                                    null,
                                    principal.getAuthorities()
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

            } catch (Exception e) {
                log.warn("{} - JWT 파싱 실패: {}", req.getRequestURI(), e.getMessage());
            }
        }

        chain.doFilter(req, res);
    }

    private String resolveBearer(HttpServletRequest req) {
        String auth = req.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        return null;
    }

    private String resolveFromCookie(HttpServletRequest req, String name) {
        if (req.getCookies() == null) return null;
        for (var c : req.getCookies()) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }
}
