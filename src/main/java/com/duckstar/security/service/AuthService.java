package com.duckstar.security.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.AuthHandler;
import com.duckstar.apiPayload.exception.handler.MemberHandler;
import com.duckstar.domain.Member;
import com.duckstar.domain.Quarter;
import com.duckstar.domain.Survey;
import com.duckstar.domain.Week;
import com.duckstar.domain.enums.CommentStatus;
import com.duckstar.domain.enums.SurveyStatus;
import com.duckstar.domain.enums.SurveyType;
import com.duckstar.domain.mapping.surveyVote.SurveyVoteSubmission;
import com.duckstar.domain.mapping.weeklyVote.EpisodeStar;
import com.duckstar.domain.mapping.weeklyVote.WeekVoteSubmission;
import com.duckstar.repository.AnimeComment.AnimeCommentRepository;
import com.duckstar.repository.EpisodeStar.EpisodeStarRepository;
import com.duckstar.repository.Reply.ReplyRepository;
import com.duckstar.repository.SurveyRepository;
import com.duckstar.repository.SurveyVoteSubmission.SurveyVoteSubmissionRepository;
import com.duckstar.repository.WeekVoteSubmission.WeekVoteSubmissionRepository;
import com.duckstar.security.domain.MemberToken;
import com.duckstar.security.jwt.JwtTokenProvider;
import com.duckstar.security.providers.google.GoogleApiClient;
import com.duckstar.security.providers.kakao.KakaoApiClient;
import com.duckstar.security.providers.naver.NaverApiClient;
import com.duckstar.security.providers.naver.NaverTokenResponse;
import com.duckstar.security.repository.MemberRepository;
import com.duckstar.security.repository.MemberTokenRepository;
import com.duckstar.service.WeekService;
import com.duckstar.web.support.VoteCookieManager;
import feign.FeignException;
import io.jsonwebtoken.Claims;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final GoogleApiClient googleApiClient;
    private final NaverApiClient naverApiClient;
    private final WeekService weekService;
    private final EpisodeStarRepository episodeStarRepository;

    private static final String BASE_VOTE_COOKIE = "vote_cookie_id";
    private static final String BASE_SURVEY_COOKIE = "survey_cookie_id";

    private final SurveyRepository surveyRepository;
    private final SurveyVoteSubmissionRepository surveyVoteSubmissionRepository;

    @Value("${app.cookie.same-site}")
    private String sameSite;

    @Value("${app.cookie.secure}")
    private boolean secureCookie;

    @Value("${app.kakao.admin-key}")
    private String adminKey;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String naverClientId;

    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String naverClientSecret;

    @Transactional
    public boolean saveTokenAndMigrateVote(
            HttpServletRequest request,
            HttpServletResponse response,
            Long memberId,
            String refreshToken
    ) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 1. Refresh Token 저장 (회전 고려)
        memberTokenRepository.save(
                MemberToken.create(
                        member,
                        refreshToken,
                        LocalDateTime.now().plusDays(1)
                )
        );

        // 2. 비회원 투표 마이그레이션 -> 회원의 투표로 저장
        // 2-1. 주간 투표
        Week week = weekService.getCurrentWeek();

        //=== 2-1. 월 18시 ~ 화 15시에는 지난 주차와 공존하는 경우 있으므로 확인 ===//
        LocalDateTime hybridStart = week.getStartDateTime();  // 이번 주 월요일 18시
        LocalDateTime hybridEnd = hybridStart.with(DayOfWeek.TUESDAY).withHour(15).withMinute(0);  // 화요일 15시
        boolean isHybrid = !LocalDateTime.now().isBefore(hybridStart) && LocalDateTime.now().isBefore(hybridEnd);

        boolean isWeekVoteMigrated;
        if (isHybrid) {
            boolean isCurrentMigrated = migrateWeekVoteSubmission(
                    request,
                    response,
                    week,
                    member
            );

            Week lastWeek = weekService.getWeekByTime(week.getStartDateTime().minusWeeks(1));
            boolean isLastWeekMigrated = migrateWeekVoteSubmission(
                    request,
                    response,
                    lastWeek,
                    member
            );

            isWeekVoteMigrated = isCurrentMigrated || isLastWeekMigrated;
        } else {
            isWeekVoteMigrated = migrateWeekVoteSubmission(
                    request,
                    response,
                    week,
                    member
            );
        }

        // 2-2. 서베이 투표
        List<Survey> surveys = surveyRepository.findAllByStatus(SurveyStatus.OPEN);

        boolean isSurveyVoteMigrated = false;
        if (!surveys.isEmpty()) {
            isSurveyVoteMigrated = surveys.stream()
                    .map(s -> migrateSurveyVoteSubmission(
                            request,
                            response,
                            s,
                            member
                    )).toList()
                    .contains(true);
        }

        return isWeekVoteMigrated || isSurveyVoteMigrated;
    }

    private boolean migrateWeekVoteSubmission(
            HttpServletRequest request,
            HttpServletResponse response,
            Week week,
            Member member
    ) {
        Quarter quarter = week.getQuarter();
        String voteCookieId = voteCookieManager.readCookie(
                request,
                quarter.getYearValue(),
                quarter.getQuarterValue(),
                week.getWeekValue()
        );
        if (voteCookieId == null || voteCookieId.isBlank()) {
            // 쿠키 없는 경우 스킵
            return false;
        }

        Long weekId = week.getId();
        Optional<WeekVoteSubmission> localSubmissionOpt =
                weekVoteSubmissionRepository.findLocalSubmission(weekId, voteCookieId);
        if (localSubmissionOpt.isEmpty()) {
            // 비로그인 투표 기록 없는 경우 스킵
            return false;
        }

        boolean isMigrated = false;
        WeekVoteSubmission localSubmission = localSubmissionOpt.get();

        Optional<WeekVoteSubmission> memberSubmissionOpt =
                weekVoteSubmissionRepository.findByWeek_IdAndMember_Id(weekId, member.getId());
        //Case 1. 비로그인 투표 기록 ⭕️ -> 투표하지 ❌않은 멤버 로그인
        if (memberSubmissionOpt.isEmpty()) {
            // ** 마이그레이션 ** //
            localSubmission.setMember(
                    member,
                    voteCookieManager.toPrincipalKey(member.getId(), null)
            );

            isMigrated = true;

        //Case 2. 비로그인 투표 기록 ⭕️ -> 다른 기기에서 ⭕투표한 멤버 로그인
        //  - 다른 기기에서 투표한 멤버의 기록에 비로그인 투표 기록을 UPSERT
        } else {
            WeekVoteSubmission memberSubmission = memberSubmissionOpt.get();

            Map<Long, EpisodeStar> memberEpisodeStarMap =
                    episodeStarRepository.findAllByWeekVoteSubmission_Id(memberSubmission.getId())
                    .stream()
                    .collect(Collectors.toMap(
                            es -> es.getEpisode().getId(),
                            es -> es
                    ));

            List<EpisodeStar> localEpisodeStars =
                    episodeStarRepository.findAllByWeekVoteSubmission_Id(localSubmission.getId())
                            .stream()
                            .filter(es -> es.getStarScore() != null)  // 로컬에서 회수한 별점은 제외
                            .toList();

            if (!localEpisodeStars.isEmpty()) {
                List<Long> deleteIds = new ArrayList<>();
                for (EpisodeStar localEpisodeStar : localEpisodeStars) {
                    // 이미 멤버가 투표한 적이 있는 후보인가?
                    EpisodeStar memberEpisodeStar =
                            memberEpisodeStarMap.get(localEpisodeStar.getEpisode().getId());
                    if (memberEpisodeStar != null) {  // 투표한 적이 있음
                        // 비로그인의 투표 점수로 업데이트
                        memberEpisodeStar.setStarScore(localEpisodeStar.getStarScore());
                        // 비로그인 투표는 삭제
                        deleteIds.add(localEpisodeStar.getId());
                    } else {
                        // 새로운 후보에 대한 투표라면, 멤버의 submission 으로 전환
                        localEpisodeStar.setWeekVoteSubmission(memberSubmission);
                    }
                }
                isMigrated = true;

                episodeStarRepository.deleteAllById(deleteIds);
            }
            weekVoteSubmissionRepository.delete(localSubmission);
        }

        // 마지막엔 역할을 다 한 쿠키를 반드시 삭제
        expireCookie(response,
                BASE_VOTE_COOKIE + "_" +
                        quarter.getYearValue() +
                        "Q" + quarter.getQuarterValue() +
                        "W" + week.getWeekValue()
        );
        return isMigrated;
    }

    @Transactional
    public boolean migrateSurveyVoteSubmission(
            HttpServletRequest request,
            HttpServletResponse response,
            Survey survey,
            Member member
    ) {
        SurveyType surveyType = survey.getSurveyType();
        String cookieId = voteCookieManager.readCookie(request, surveyType);
        if (cookieId == null || cookieId.isBlank()) {
            return false;
        }

        Long surveyId = survey.getId();
        Optional<SurveyVoteSubmission> localSubmissionOpt =
                surveyVoteSubmissionRepository.findLocalSubmission(surveyId, cookieId);
        if (localSubmissionOpt.isEmpty()) {
            // 비로그인 투표 기록 없는 경우 스킵
            return false;
        }

        boolean isMigrated = false;
        SurveyVoteSubmission localSubmission = localSubmissionOpt.get();

        Optional<SurveyVoteSubmission> memberSubmissionOpt =
                surveyVoteSubmissionRepository.findBySurvey_IdAndMember_Id(surveyId, member.getId());
        //Case 1. 비로그인 투표 기록 ⭕️ -> 투표하지 ❌않은 멤버 로그인
        if (memberSubmissionOpt.isEmpty()) {
            localSubmission.setMember(
                    member,
                    voteCookieManager.toPrincipalKey(member.getId(), null)
            );

            isMigrated = true;

            // Case 1에서만 - 역할을 다 한 쿠키를 삭제
            expireCookie(response, BASE_SURVEY_COOKIE + "_" + surveyType.name());

        //Case 2. 비로그인 투표 기록 ⭕️ -> 다른 기기에서 ⭕투표한 멤버 로그인
        //  - 서베이 방식에서는 기존 비로그인 투표 기록을 보존한다.
        } else { /* do nothing */ }

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
    public Long logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtTokenProvider.resolveFromCookie(request, "REFRESH_TOKEN");

        Claims claims = jwtTokenProvider.parseClaims(refreshToken);
        if (!jwtTokenProvider.isRefreshToken(claims)) {
            throw new AuthHandler(ErrorStatus.REFRESH_TOKEN_MISSING);
        }

        Optional<MemberToken> memberTokenOpt = memberTokenRepository.findByRefreshToken(refreshToken);
        if (memberTokenOpt.isEmpty()) {
            throw new AuthHandler(ErrorStatus.REFRESH_TOKEN_NOT_FOUND);
        }
        Member member = memberTokenOpt.get().getMember();

        LocalDateTime now = LocalDateTime.now();
        Week week = weekService.getWeekByTime(now);
        Optional<WeekVoteSubmission> thisWeekSubmissionOpt =
                weekVoteSubmissionRepository.findByWeek_IdAndMember_Id(week.getId(), member.getId());

        Long thisWeekSec = 0L;
        if (thisWeekSubmissionOpt.isPresent()) {
            thisWeekSec = episodeStarRepository
                    .getVoteTimeLeftForLatestEpVoted(thisWeekSubmissionOpt.get().getId());
        }

        Long lastWeekSec = 0L;
        if (thisWeekSec == 0L) {
            LocalDateTime hybridStart = week.getStartDateTime();  // 이번 주 월요일 18시
            LocalDateTime hybridEnd = hybridStart.with(DayOfWeek.TUESDAY).withHour(15).withMinute(0);  // 화요일 15시
            boolean isHybrid = !now.isBefore(hybridStart) && now.isBefore(hybridEnd);
            if (isHybrid) {
                Week lastWeek = weekService.getWeekByTime(week.getStartDateTime().minusWeeks(1));

                Optional<WeekVoteSubmission> lastWeekSubmissionOpt =
                        weekVoteSubmissionRepository.findByWeek_IdAndMember_Id(lastWeek.getId(), member.getId());

                if (lastWeekSubmissionOpt.isPresent()) {
                   lastWeekSec = episodeStarRepository
                            .getVoteTimeLeftForLatestEpVoted(lastWeekSubmissionOpt.get().getId());
                }
            }
        }

        memberTokenRepository.deleteByRefreshToken(refreshToken);

        expireCookie(response, "ACCESS_TOKEN");
        expireCookie(response, "REFRESH_TOKEN");
        expireCookie(response, "AUTH_STATUS"); // 🔑 AUTH_STATUS 쿠키도 삭제

        return thisWeekSec > 0L ? thisWeekSec : lastWeekSec;
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

        try {
            kakaoApiClient.unlink(
                    "KakaoAK " + adminKey,
                    "user_id",
                    member.getProviderId()
                    );

        } catch (FeignException e) {
            log.warn("카카오 unlink 실패 - memberId={}, 이유={}", memberId, e.getMessage());
        }

        member.withdraw();
        cleanupAfterWithdraw(response, memberId);
    }

    @Transactional
    public void withdrawGoogle(HttpServletResponse response, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        String refreshToken = member.getSocialRefreshToken();
        try {
            if (refreshToken != null) {
                MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
                body.add("token", refreshToken);
                body.add("token_type_hint", "refresh_token");

                googleApiClient.revoke(body);
                log.info("✅ 구글 계정 연결 해제 성공 - memberId={}", memberId);
            } else {
                log.info("❌ refresh_token 없음");
            }
        } catch (FeignException e) {
            log.warn("구글 unlink 실패 - memberId={}, 이유={}", member.getId(), e.getMessage());
        }

        member.withdraw();
        cleanupAfterWithdraw(response, member.getId());
    }

    @Transactional
    public void withdrawNaver(HttpServletResponse response, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        String accessToken;
        String refreshToken = member.getSocialRefreshToken();
        if (refreshToken != null) {
            // code → token 교환
            NaverTokenResponse tokenResponse = naverApiClient.refreshCode(
                    "refresh_token",
                    naverClientId,
                    naverClientSecret,
                    refreshToken
            );
            accessToken = tokenResponse.getAccess_token();
        } else {
            log.info("❌ refresh_token 없음");
            accessToken = null;
        }

        if (accessToken != null) {
            try {
                Map<String, Object> result = naverApiClient.deleteToken(
                        "delete",
                        naverClientId,
                        naverClientSecret,
                        accessToken,
                        "NAVER"
                );
                if ("success".equals(result.get("result"))) {
                    log.info("✅ 네이버 unlink 성공 - memberId={}", memberId);
                } else {
                    log.warn("⚠️ 네이버 unlink 실패 - memberId={}, 응답={}", memberId, result);
                }
            } catch (FeignException e) {
                log.warn("❌ 네이버 unlink 실패 - memberId={}, 이유={}", member.getId(), e.getMessage());
            }
        }

        member.withdraw();
        cleanupAfterWithdraw(response, member.getId());
    }

    private void cleanupAfterWithdraw(HttpServletResponse response, Long memberId) {
        memberTokenRepository.deleteAllByMember_Id(memberId);

        // 투표 기록에서 회원 정보 삭제
        weekVoteSubmissionRepository.findAllByMember_Id(memberId)
                .forEach(sub -> {
                    String cookieId = sub.getCookieId();
                    sub.setMember(null, voteCookieManager.toPrincipalKey(null, cookieId));
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
        expireCookie(response, "AUTH_STATUS"); // 🔑 AUTH_STATUS 쿠키도 삭제
    }
}
