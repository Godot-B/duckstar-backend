package com.duckstar.security.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.AuthHandler;
import com.duckstar.apiPayload.exception.handler.MemberHandler;
import com.duckstar.apiPayload.exception.handler.VoteHandler;
import com.duckstar.domain.Member;
import com.duckstar.domain.enums.CommentStatus;
import com.duckstar.domain.mapping.WeekVoteSubmission;
import com.duckstar.repository.AnimeComment.AnimeCommentRepository;
import com.duckstar.repository.Reply.ReplyRepository;
import com.duckstar.repository.WeekVoteSubmissionRepository;
import com.duckstar.security.domain.MemberToken;
import com.duckstar.security.jwt.JwtTokenProvider;
import com.duckstar.security.providers.kakao.KakaoApiClient;
import com.duckstar.security.repository.MemberRepository;
import com.duckstar.security.repository.MemberTokenRepository;
import com.duckstar.web.support.VoteCookieManager;
import feign.FeignException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberTokenRepository memberTokenRepository;
    private final MemberRepository memberRepository;
    private final AnimeCommentRepository animeCommentRepository;
    private final ReplyRepository replyRepository;

    private final JwtTokenProvider jwtTokenProvider;
    private final KakaoApiClient kakaoApiClient;
    private final WeekVoteSubmissionRepository weekVoteSubmissionRepository;
    private final VoteCookieManager voteCookieManager;

    @Value("${app.cookie.same-site}")
    private String sameSite;

    @Value("${app.cookie.secure}")
    private boolean secureCookie;

    @Value("${app.kakao.admin-key}")
    private String adminKey;

    @Transactional
    public boolean saveTokenAndMigrateVote(
            HttpServletRequest request,
            HttpServletResponse response,
            Member member,
            String refreshToken
    ) {
        // 1. Refresh Token 저장 (회전 고려)
        memberTokenRepository.save(
                MemberToken.create(
                        member,
                        refreshToken,
                        LocalDateTime.now().plusDays(1)
                )
        );

        // 2. 비회원 투표 마이그레이션 -> 회원의 투표로 저장
        String voteCookieId = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("vote_cookie_id".equals(cookie.getName())) {
                    voteCookieId = cookie.getValue();
                    break;
                }
            }
        }

        if (voteCookieId == null || voteCookieId.isBlank()) {
            // 쿠키 없는 경우 스킵
            return false;
        }

        String principalKey = voteCookieManager.toPrincipalKey(null, voteCookieId);
        Optional<WeekVoteSubmission> localSubmissionOpt =
                weekVoteSubmissionRepository.findByPrincipalKey(principalKey);
        if (localSubmissionOpt.isEmpty()) {
            // 비로그인 투표 기록 없는 경우 스킵
            return false;
        }

        boolean isMigrated = false;
        WeekVoteSubmission submission = localSubmissionOpt.get();
        if (submission.getMember() == null) {

            Optional<WeekVoteSubmission> memberSubmissionOpt =
                    weekVoteSubmissionRepository.findByMember_Id(member.getId());
            //Case 1. 비로그인 투표 기록 ⭕️ -> 투표하지 않은 멤버 로그인
            if (memberSubmissionOpt.isEmpty()) {
                // ** 마이그레이션 ** //
                submission.setMember(
                        member,
                        voteCookieManager.toPrincipalKey(member.getId(), null)
                );

                isMigrated = true;
            }

            //Case 2. 비로그인 투표 기록 ⭕ -> 이미 투표한 🗳 멤버 로그인: 말 없이 쿠키 삭제
            expireCookie(response, "vote_cookie_id");
        }

        return isMigrated;
    }

    @Transactional
    public ResponseEntity<Map<String, String>> refresh(HttpServletRequest request) {

        String refreshToken = jwtTokenProvider.resolveFromCookie(request, "REFRESH_TOKEN");

        Claims claims = jwtTokenProvider.parseClaims(refreshToken);
        if (!jwtTokenProvider.isRefreshToken(claims)) {
            throw new AuthHandler(ErrorStatus.REFRESH_TOKEN_MISSING);
        }

        MemberToken memberToken = memberTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new AuthHandler(ErrorStatus.REFRESH_TOKEN_NOT_FOUND));

        if (memberToken.isExpired()) {
            throw new AuthHandler(ErrorStatus.REFRESH_TOKEN_EXPIRED);
        }

        Member member = memberToken.getMember();

        // 회전 처리
        memberTokenRepository.delete(memberToken);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(member.getId(), member.getRole());
        memberTokenRepository.save(
                MemberToken.create(
                        member,
                        newRefreshToken,
                        LocalDateTime.now().plusDays(7)
                )
        );

        String newAccessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getRole());

        Map<String, String> response = Map.of(
                "accessToken", newAccessToken,
                "refreshToken", newRefreshToken
        );

        return ResponseEntity.ok(response);
    }

    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtTokenProvider.resolveFromCookie(request, "REFRESH_TOKEN");

        Claims claims = jwtTokenProvider.parseClaims(refreshToken);
        if (!jwtTokenProvider.isRefreshToken(claims)) {
            throw new AuthHandler(ErrorStatus.REFRESH_TOKEN_MISSING);
        }

        memberTokenRepository.deleteByRefreshToken(refreshToken);

        expireCookie(response, "ACCESS_TOKEN");
        expireCookie(response, "REFRESH_TOKEN");
    }

    private void expireCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite(sameSite)
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @Transactional
    public void withdrawKakao(HttpServletResponse response, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        member.withdraw();

        try {
            kakaoApiClient.unlink(
                    "KakaoAK " + adminKey,
                    "user_id",
                    member.getProviderId()
                    );

        } catch (FeignException e) {
            log.warn("카카오 unlink 실패 - memberId={}, 이유={}", memberId, e.getMessage());
        }

        memberTokenRepository.deleteAllByMember_Id(memberId);

        // 투표 기록에서 회원 정보 삭제
        List<WeekVoteSubmission> submissions = weekVoteSubmissionRepository.findAllByMember_Id(memberId);
        submissions.forEach(submission -> {
            String cookieId = submission.getCookieId();
            submission.setMember(null, voteCookieManager.toPrincipalKey(null, cookieId));
        });

        // 애니 댓글 삭제
        animeCommentRepository.findAllByAuthor_Id(memberId)
                .forEach(ac -> ac.setStatus(CommentStatus.DELETED));

        // 캐릭터 댓글 삭제

        // 답글 삭제
        replyRepository.findAllByAuthor_Id(memberId)
                .forEach(r -> r.setStatus(CommentStatus.DELETED));

        expireCookie(response, "ACCESS_TOKEN");
        expireCookie(response, "REFRESH_TOKEN");
    }
}
