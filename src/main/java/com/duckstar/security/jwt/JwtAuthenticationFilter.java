package com.duckstar.security.jwt;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.MemberHandler;
import com.duckstar.domain.Member;
import com.duckstar.security.MemberPrincipal;
import com.duckstar.security.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. 헤더에서 Bearer 토큰 추출
        String token = resolveToken(request);

        if (token != null) {
            try {

                // 2. 토큰 검증
                if (jwtTokenProvider.validateToken(token)) {
                    Claims claims = jwtTokenProvider.parseClaims(token);

                    if (jwtTokenProvider.isAccessToken(claims)) {
                        Long memberId = Long.valueOf(claims.getSubject());

                        // 3. DB 조회 (권한 포함)
                        Member member = memberRepository.findById(memberId)
                                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

                        MemberPrincipal principal = MemberPrincipal.of(member);

                        // 4. Authentication 객체 생성
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                principal.getAuthorities()
                        );

                        authentication.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );

                        // 5. SecurityContext에 저장
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("✅ JWT 인증 성공 - memberId={}", memberId);
                    }
                }

            } catch (Exception e) {
                log.warn("❌ JWT 인증 실패: {} - URI: {}", e.getMessage(), request.getRequestURI());
            }
        }

        // 6. 필터 체인 계속 진행
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        // 1순위: Authorization 헤더
        String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        // 2순위: 쿠키
        return jwtTokenProvider.resolveFromCookie(request, "ACCESS_TOKEN");
    }
}
