package com.duckstar.service.VoteService;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.*;
import com.duckstar.domain.*;
import com.duckstar.domain.mapping.surveyVote.SurveyCandidate;
import com.duckstar.domain.mapping.surveyVote.SurveyVote;
import com.duckstar.domain.mapping.surveyVote.SurveyVoteSubmission;
import com.duckstar.repository.AnimeRepository;
import com.duckstar.repository.SurveyCandidate.SurveyCandidateRepository;
import com.duckstar.repository.SurveyRepository;
import com.duckstar.domain.enums.*;
import com.duckstar.domain.mapping.comment.AnimeComment;
import com.duckstar.domain.mapping.weeklyVote.Episode;
import com.duckstar.domain.mapping.weeklyVote.EpisodeStar;
import com.duckstar.domain.mapping.weeklyVote.WeekVoteSubmission;
import com.duckstar.repository.AnimeComment.AnimeCommentRepository;
import com.duckstar.repository.Episode.EpisodeRepository;
import com.duckstar.repository.EpisodeStar.EpisodeStarRepository;
import com.duckstar.repository.SurveyVote.SurveyVoteRepository;
import com.duckstar.repository.SurveyVoteSubmission.SurveyVoteSubmissionRepository;
import com.duckstar.repository.Week.WeekRepository;
import com.duckstar.repository.WeekVoteSubmission.WeekVoteSubmissionRepository;
import com.duckstar.security.repository.MemberRepository;
import com.duckstar.security.service.ShadowBanService;
import com.duckstar.service.WeekService;
import com.duckstar.web.support.Hasher;
import com.duckstar.web.support.IdentifierExtractor;
import com.duckstar.web.support.VoteCookieManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.duckstar.web.dto.SurveyRequestDto.*;
import static com.duckstar.web.dto.VoteRequestDto.*;
import static com.duckstar.web.dto.VoteResponseDto.*;

@Service
@RequiredArgsConstructor
@Transactional
public class VoteCommandServiceImpl implements VoteCommandService {
    private final WeekVoteSubmissionRepository submissionRepository;
    private final MemberRepository memberRepository;
    private final EpisodeRepository episodeRepository;
    private final EpisodeStarRepository episodeStarRepository;
    private final WeekRepository weekRepository;
    private final AnimeCommentRepository animeCommentRepository;

    private final VoteCookieManager voteCookieManager;
    private final IdentifierExtractor identifierExtractor;
    private final Hasher hasher;

    private final WeekService weekService;
    private final ShadowBanService shadowBanService;
    private final SurveyRepository surveyRepository;
    private final SurveyVoteSubmissionRepository surveyVoteSubmissionRepository;
    private final SurveyCandidateRepository surveyCandidateRepository;
    private final SurveyVoteRepository surveyVoteRepository;
    private final AnimeRepository animeRepository;

    @Override
    public void voteSurvey(
            AnimeVoteRequest request,
            Long memberId,
            HttpServletRequest requestRaw,
            HttpServletResponse responseRaw
    ) {
        //=== 서베이 존재, 투표 가능 검사 ===//
        Long surveyId = request.getSurveyId();
        Survey survey = surveyRepository.findById(surveyId).orElseThrow(() ->
                new VoteHandler(ErrorStatus.SURVEY_NOT_FOUND));
        if (survey.getStatus() != SurveyStatus.OPEN) {
            throw new VoteHandler(ErrorStatus.VOTE_CLOSED);
        }

        //=== 멤버와 쿠키 ID 찾기 ===//
        Gender gender = request.getGender();
        AgeGroup ageGroup = request.getAgeGroup();
        Member member;
        String cookieId;
        if (memberId != null) {
            member = memberRepository.findById(memberId).orElseThrow(() ->
                    new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
            cookieId = null;

            if (member != null) {
                member.setGender(gender);
                member.setAgeGroup(ageGroup);
            }
        } else {
            member = null;
            cookieId = voteCookieManager.ensureSurveyCookie(
                    requestRaw,
                    responseRaw,
                    survey.getSurveyType(),
                    survey.getEndDateTime()
            );
        }

        //=== Submission, 중복 투표 방지 ===//
        String ip = identifierExtractor.extract(requestRaw);
        String ipHash = hasher.hash(ip);
        // 일단 기록용으로만 둔다
        String userAgent = identifierExtractor.safeUserAgent(requestRaw);
        String fpHash = identifierExtractor.safeFpHash(requestRaw);

        SurveyVoteSubmission submission = SurveyVoteSubmission.create(
                survey,
                voteCookieManager.toPrincipalKey(memberId, cookieId),
                member,
                cookieId,
                ipHash,
                userAgent,
                fpHash,
                gender,
                ageGroup
        );
        SurveyVoteSubmission savedSubmission;
        try {
            savedSubmission = surveyVoteSubmissionRepository.save(submission);
        } catch (DataIntegrityViolationException e) {
            throw new VoteHandler(ErrorStatus.ALREADY_VOTED);
        }

        //=== 실제 투표지 검사: 후보 유효성(중복 포함됨, 이번 주 후보 아님) ===//
        List<BallotRequestDto> ballotRequests = request.getBallotRequests();

        List<Long> candidateIds = ballotRequests.stream()
                .map(BallotRequestDto::getCandidateId)
                .toList();

        Set<Long> uniq = new HashSet<>(candidateIds);
        if (uniq.size() != candidateIds.size()) {
            throw new VoteHandler(ErrorStatus.DUPLICATE_CANDIDATE_INCLUDED);
        }

        Set<Long> valid = new HashSet<>(
                surveyCandidateRepository.findValidIdsForSurvey(surveyId, candidateIds)
        );
        if (valid.size() != uniq.size()) {
            throw new VoteHandler(ErrorStatus.INVALID_CANDIDATE_INCLUDED);
        }

        //=== 저장 ===//
        List<SurveyVote> rows = new ArrayList<>();
        for (BallotRequestDto dto : ballotRequests) {
            SurveyCandidate candidate =
                    surveyCandidateRepository.getReferenceById(dto.getCandidateId()); // 프록시 객체 반환

            SurveyVote surveyVote = SurveyVote.create(
                    savedSubmission,
                    candidate,
                    dto.getBallotType()
            );
            rows.add(surveyVote);
        }

        surveyVoteRepository.saveAll(rows);
    }

    @Override
    public void revoteSurvey(
            Long submissionId,
            AnimeRevoteRequest request,
            Long memberId
    ) {
        // 회원 검사
        if (memberId == null) {
            throw new VoteHandler(ErrorStatus.MEMBER_NOT_FOUND);
        }
        Member member = memberRepository.findById(memberId).orElseThrow(() ->
                new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        Gender gender = request.getGender();
        AgeGroup ageGroup = request.getAgeGroup();
        member.setGender(gender);
        member.setAgeGroup(ageGroup);

        //=== 서베이 존재, 투표 가능 검사 ===//
        Long surveyId = request.getSurveyId();
        Survey survey = surveyRepository.findById(surveyId).orElseThrow(() ->
                new VoteHandler(ErrorStatus.SURVEY_NOT_FOUND));
        if (survey.getStatus() != SurveyStatus.OPEN) {
            throw new VoteHandler(ErrorStatus.VOTE_CLOSED);
        }

        //=== 실제 투표지 검사: 후보 유효성(중복 포함됨, 이번 주 후보 아님) ===//
        List<BallotRequestDto> addRequests = request.getAdded();
        List<Long> addReqIds = List.of();
        if (addRequests != null && !addRequests.isEmpty()) {
            addReqIds = addRequests.stream()
                    .map(BallotRequestDto::getCandidateId)
                    .toList();
        }
        List<BallotRequestDto> updateRequests = request.getUpdated();
        List<Long> updateReqIds = List.of();
        if (updateRequests != null && !updateRequests.isEmpty()) {
            updateReqIds = updateRequests.stream()
                    .map(BallotRequestDto::getCandidateId)
                    .toList();
        }
        List<BallotRequestDto> removed = request.getRemoved();
        List<Long> removeReqIds = List.of();
        if (removed != null && !removed.isEmpty()) {
            removeReqIds = request.getRemoved().stream()
                    .map(BallotRequestDto::getCandidateId)
                    .toList();
        }

        List<Long> allIds = Stream.of(addReqIds, removeReqIds, updateReqIds)
                .flatMap(List::stream)
                .toList();

        if (allIds.size() != new HashSet<>(allIds).size()) {
            throw new VoteHandler(ErrorStatus.DUPLICATE_CANDIDATE_INCLUDED);
        }

        Set<Long> valid = new HashSet<>(
                surveyCandidateRepository.findValidIdsForSurvey(surveyId, allIds)
        );
        if (valid.size() != allIds.size()) {
            throw new VoteHandler(ErrorStatus.INVALID_CANDIDATE_INCLUDED);
        }

        //=== 삭제 ===//
        if (!removeReqIds.isEmpty()) {
            surveyVoteRepository.deleteAllBySurveyVoteSubmission_IdAndSurveyCandidate_IdIn(
                    submissionId, removeReqIds);
        }

        SurveyVoteSubmission submission = surveyVoteSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new VoteHandler(ErrorStatus.SUBMISSION_NOT_FOUND));

        //=== 수정 ===//
        if (!updateReqIds.isEmpty()) {
            List<SurveyVote> votesToUpdate = surveyVoteRepository
                    .findAllBySurveyVoteSubmission_IdAndSurveyCandidate_IdIn(submission.getId(), updateReqIds);

            Map<Long, BallotType> map = updateRequests.stream()
                    .collect(Collectors.toMap(
                            BallotRequestDto::getCandidateId,
                            BallotRequestDto::getBallotType
                    ));

            for (SurveyVote vote : votesToUpdate) {
                vote.updateScore(map.get(vote.getSurveyCandidate().getId()));
            }
        }

        //=== 추가 ===//
        if (!addReqIds.isEmpty()) {
            List<SurveyVote> rows = new ArrayList<>();
            for (BallotRequestDto dto : addRequests) {
                SurveyCandidate candidate =
                        surveyCandidateRepository.getReferenceById(dto.getCandidateId()); // 프록시 객체 반환

                SurveyVote surveyVote = SurveyVote.create(
                        submission,
                        candidate,
                        dto.getBallotType()
                );
                rows.add(surveyVote);
            }
            surveyVoteRepository.saveAll(rows);
        }

        submission.setUpdatedAt(LocalDateTime.now());
    }

    @Override
    public VoteResultDto voteOrUpdateStar(
            StarRequestDto request,
            Long memberId,
            HttpServletRequest requestRaw,
            HttpServletResponse responseRaw
    ) {
        //=== 투표 유효성( VOTING_WINDOW 상태인지 ) ===//
        Long episodeId = request.getEpisodeId();
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new EpisodeHandler(ErrorStatus.EPISODE_NOT_FOUND));

        if (episode.getEvaluateState() != EpEvaluateState.VOTING_WINDOW) {
            throw new VoteHandler(ErrorStatus.VOTE_CLOSED);
        }

        // ** 방영된 에피소드가 속한 주
        Week inlcudedWeek = weekService.getWeekByTime(episode.getScheduledAt());

        //=== 멤버와 쿠키 ID 찾기 ===//
        Member member;
        String cookieId;
        if (memberId != null) {
            member = memberRepository.findById(memberId).orElseThrow(() ->
                    new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
            cookieId = null;
        } else {
            member = null;
            Quarter quarter = inlcudedWeek.getQuarter();
            cookieId = voteCookieManager.ensureVoteCookie(
                    requestRaw,
                    responseRaw,
                    quarter.getYearValue(),
                    quarter.getQuarterValue(),
                    inlcudedWeek.getWeekValue()
            );
        }

        Long episodeStarId = request.getEpisodeStarId();
        EpisodeStar episodeStar;
        if (episodeStarId != null) {
            //=== 수정 ===//
            episodeStar = episodeStarRepository.findById(episodeStarId).orElseThrow(() ->
                    new VoteHandler(ErrorStatus.STAR_NOT_FOUND));
            boolean isBlocked = episodeStar.getWeekVoteSubmission().isBlocked();
            Integer newStarScore = request.getStarScore();

            //=== 표 수정 권한 검증 ===//
            boolean isProperEpisode = episodeStar.getEpisode()
                    .equals(episode);

            String principalKey = voteCookieManager.toPrincipalKey(memberId, cookieId);
            boolean isProperVoter = episodeStar.getWeekVoteSubmission().getPrincipalKey()
                    .equals(principalKey);

            if (!isProperEpisode || !isProperVoter) {
                throw new AuthHandler(ErrorStatus.STAR_UNAUTHORIZED);
            }

            // 별점 반영
            episodeStar.updateStarScore(isBlocked, newStarScore);

        } else {
            //=== 제출 및 투표 ===//
            episodeStar = createOrGetSubmissionAndCreateOrUpdateStar(
                    inlcudedWeek,
                    episode,
                    member,
                    cookieId,
                    requestRaw,
                    request.getStarScore()
            );
        }

        return VoteResultDto.builder()
                .voterCount(episode.getVoterCount())
                .info(
                        StarInfoDto.of(
                                episodeStar.getWeekVoteSubmission().isBlocked(),
                                episodeStar,
                                episode
                        )
                )
                .build();
    }

    private EpisodeStar createOrGetSubmissionAndCreateOrUpdateStar(
            Week week,
            Episode episode,
            Member member,
            String cookieId,
            HttpServletRequest requestRaw,
            Integer starScore
    ) {
        //=== 제출 정보 찾기 ===//
        Long memberId = member != null ? member.getId() : null;
        String principalKey = voteCookieManager.toPrincipalKey(memberId, cookieId);
        Optional<WeekVoteSubmission> submissionOpt =
                submissionRepository.findByWeek_IdAndPrincipalKey(week.getId(), principalKey);

        WeekVoteSubmission submission = submissionOpt.orElseGet(() -> {
            String ip = identifierExtractor.extract(requestRaw);
            String ipHash = hasher.hash(ip);

            boolean isBanned = shadowBanService.isBanned(ipHash);

            // 일단 기록용으로만 둔다
            String userAgent = identifierExtractor.safeUserAgent(requestRaw);
            String fpHash = identifierExtractor.safeFpHash(requestRaw);

            return submissionRepository.save(WeekVoteSubmission.create(
                    isBanned,
                    week,
                    member,
                    cookieId,
                    ipHash,
                    userAgent,
                    fpHash,
                    voteCookieManager.toPrincipalKey(memberId, cookieId),
                    ContentType.ANIME
            ));
        });

        //=== 별점 반영 ===//
        Optional<EpisodeStar> episodeStarOpt = episodeStarRepository
                .findByEpisode_IdAndWeekVoteSubmission_Id(episode.getId(), submission.getId());

        boolean isBlocked = submission.isBlocked();  // 차단 유저는 통계 반영 X

        EpisodeStar episodeStar;
        if (episodeStarOpt.isPresent()) {
            episodeStar = episodeStarOpt.get();
            episodeStar.updateStarScore(isBlocked, starScore);
        } else {
            episodeStar = episodeStarRepository.save(
                    EpisodeStar.create(
                            isBlocked,
                            submission,
                            episode,
                            starScore
                    )
            );
        }

        return episodeStar;
    }

    @Override
    public VoteFormResultDto voteOrUpdateStarWithLoginAndComment(
            LateStarRequestDto request,
            Long memberId,
            HttpServletRequest requestRaw
    ) {
        if (memberId == null) {
            throw new AuthHandler(ErrorStatus.LATE_STAR_UNAUTHORIZED);
        }
        Member member = memberRepository.findById(memberId).orElseThrow(() ->
                new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        //=== 투표 유효성( VOTING_WINDOW 또는 LOGIN_REQUIRED 상태인지 ) ===//
        Episode episode = episodeRepository.findById(request.getEpisodeId()).orElseThrow(() ->
                new EpisodeHandler(ErrorStatus.EPISODE_NOT_FOUND));
        EpEvaluateState state = episode.getEvaluateState();

        boolean isValidTime =
                state == EpEvaluateState.VOTING_WINDOW
                        || state == EpEvaluateState.LOGIN_REQUIRED;
        if (!isValidTime) {
            throw new VoteHandler(ErrorStatus.VOTE_CLOSED);
        }

        // ** 방영된 에피소드가 속한 주
        Week inlcudedWeek = weekService.getWeekByTime(episode.getScheduledAt());

        Long episodeStarId = request.getEpisodeStarId();
        EpisodeStar episodeStar;
        if (episodeStarId != null) {
            //=== 수정 ===//
            episodeStar = episodeStarRepository.findById(episodeStarId).orElseThrow(() ->
                    new VoteHandler(ErrorStatus.STAR_NOT_FOUND));
            boolean isBlocked = episodeStar.getWeekVoteSubmission().isBlocked();
            Integer newStarScore = request.getStarScore();

            //=== 표 수정 권한 검증 ===//
            boolean isProperEpisode = episodeStar.getEpisode()
                    .equals(episode);

            String principalKey = voteCookieManager.toPrincipalKey(memberId, null);
            boolean isProperVoter = episodeStar.getWeekVoteSubmission().getPrincipalKey()
                    .equals(principalKey);

            if (!isProperEpisode || !isProperVoter) {
                throw new AuthHandler(ErrorStatus.STAR_UNAUTHORIZED);
            }

            Integer oldStarScore = episodeStar.getStarScore();

            // 다를 때만 별점 반영 (updated_at 제공 때문에)
            if (!Objects.equals(oldStarScore, newStarScore)) {
                episodeStar.updateStarScore(isBlocked, newStarScore);
            }

        } else {
            //=== 제출 및 투표 ===//
            episodeStar = createOrGetSubmissionAndCreateOrUpdateStar(
                    inlcudedWeek,
                    episode,
                    member,
                    null,
                    requestRaw,
                    request.getStarScore()
            );
        }

        AnimeComment comment;
        Optional<AnimeComment> commentOpt =
                animeCommentRepository.findByEpisodeStar_Id(episodeStar.getId());
        if (commentOpt.isPresent() && commentOpt.get().getStatus() == CommentStatus.NORMAL) {
            //=== 수정 ===//
            comment = commentOpt.get();
            comment.updateBody(request.getBody());

        } else {
            //=== 댓글 저장 ===//
            int voteCount = episodeStarRepository
                    .countAllByEpisode_Anime_IdAndWeekVoteSubmission_Member_Id(
                            episode.getAnime().getId(),
                            member.getId()
                    );

            AnimeComment animeComment = AnimeComment.create(
                    episode.getAnime(),
                    episode,
                    member,
                    true,  // 댓글에 화수 명시
                    voteCount,
                    null,
                    request.getBody()
            );
            animeComment.setEpisodeStar(episodeStar);  // episode_star 관계 셋팅

            comment = animeCommentRepository.save(animeComment);
        }

        // 별점 통계
        StarInfoDto info = StarInfoDto.of(
                episodeStar.getWeekVoteSubmission().isBlocked(),
                episodeStar,
                episode
        );

        return VoteFormResultDto.builder()
                .isLateParticipating(episodeStar.isLateParticipating())
                .voterCount(episode.getVoterCount())
                .info(info)
                .voteUpdatedAt(episodeStar.getUpdatedAt())
                .commentId(comment.getId())
                .body(comment.getBody())
                .build();
    }
    
    @Override
    public void withdrawStar(
            Long episodeId,
            Long episodeStarId,
            Long memberId,
            HttpServletRequest requestRaw,
            HttpServletResponse responseRaw
    ) {
        //=== 투표 유효성( VOTING_WINDOW 상태인지 ) ===//
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new EpisodeHandler(ErrorStatus.EPISODE_NOT_FOUND));

        if (episode.getEvaluateState() != EpEvaluateState.VOTING_WINDOW) {
            throw new VoteHandler(ErrorStatus.VOTE_CLOSED);
        }

        // ** 방영된 에피소드가 속한 주
        Week inlcudedWeek = weekService.getWeekByTime(episode.getScheduledAt());

        //=== 멤버와 쿠키 ID 찾기 ===//
        String cookieId;
        if (memberId != null) {
            memberRepository.findById(memberId).orElseThrow(() ->
                    new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
            cookieId = null;
        } else {
            Quarter quarter = inlcudedWeek.getQuarter();
            cookieId = voteCookieManager.ensureVoteCookie(
                    requestRaw,
                    responseRaw,
                    quarter.getYearValue(),
                    quarter.getQuarterValue(),
                    inlcudedWeek.getWeekValue()
            );
        }

        //=== 표 수정 권한 검증 ===//
        EpisodeStar episodeStar = episodeStarRepository.findById(episodeStarId).orElseThrow(() ->
                new VoteHandler(ErrorStatus.STAR_NOT_FOUND));

        boolean isProperEpisode = episodeStar.getEpisode()
                .equals(episode);

        String principalKey = voteCookieManager.toPrincipalKey(memberId, cookieId);
        boolean isProperVoter = episodeStar.getWeekVoteSubmission().getPrincipalKey()
                .equals(principalKey);

        if (!isProperEpisode || !isProperVoter) {
            throw new AuthHandler(ErrorStatus.STAR_UNAUTHORIZED);
        }

        // 별점 회수
        episodeStar.withdrawScore();
    }

    @Override
    public void refreshEpisodeStatsByWeekId(Long weekId) {
        Week lastWeek = weekRepository.findWeekById(weekId).orElseThrow(() ->
                new WeekHandler(ErrorStatus.WEEK_NOT_FOUND));

        //=== 회수된 표 제외 === //
        List<EpisodeStar> allEpisodeStars = episodeStarRepository.findAllEligibleByWeekId(weekId);

        Map<Long, List<EpisodeStar>> episodeStarMap = allEpisodeStars.stream()
                .collect(Collectors.groupingBy(es -> es.getEpisode().getId()));

        //=== 이번 주 휴방 아닌 에피소드들 - 표 집계 ===//
        List<Episode> episodes = episodeRepository
                .findAllByScheduledAtGreaterThanEqualAndScheduledAtLessThan(
                        lastWeek.getStartDateTime(), lastWeek.getEndDateTime())
                .stream()
                .filter(e -> !e.isBreak())
                .toList();

        for (Episode episode : episodes) {
            List<EpisodeStar> thisEpisodeStars =
                    episodeStarMap.get(episode.getId());

            if (thisEpisodeStars != null && !thisEpisodeStars.isEmpty())  {
                int voterCount = thisEpisodeStars.size();

                int[] scores = new int[10];

                for (EpisodeStar episodeStar : thisEpisodeStars) {
                    Integer starScore = episodeStar.getStarScore();
                    int idx = starScore - 1;
                    scores[idx] += 1;
                }

                episode.setStats(voterCount, scores);
            }
        }
    }

    @Override
    public SurveyCommentDto postCommentBySurvey(SurveyCommentRequestDto request, Long memberId) {
        Member author = memberRepository.findById(memberId).orElseThrow(() ->
                new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        Long animeId = request.getAnimeId();
        Anime anime = animeRepository.findById(animeId).orElseThrow(() ->
                new AnimeHandler(ErrorStatus.ANIME_NOT_FOUND));

        int voteCount = episodeStarRepository
                .countAllByEpisode_Anime_IdAndWeekVoteSubmission_Member_Id(
                        animeId,
                        memberId
                );

//        String imageUrl = null;
//        MultipartFile image = request.getAttachedImage();
//        if (image != null && !image.isEmpty()) {
//            imageUrl = s3Uploader.uploadWithUUID(image, "comments");
//        }

        AnimeComment animeComment = AnimeComment.create(
                anime,
                null,
                author,
                false,
                voteCount,
                null,
                request.getBody()
        );
        SurveyCandidate candidate = surveyCandidateRepository.findById(request.getCandidateId()).orElseThrow(() ->
                new CommentHandler(ErrorStatus.ANIME_CANDIDATE_NOT_FOUND));
        animeComment.setSurveyCandidate(candidate);

        AnimeComment saved = animeCommentRepository.save(animeComment);

        return SurveyCommentDto.builder()
                .commentId(saved.getId())
                .commentCreatedAt(saved.getCreatedAt())
                .body(saved.getBody())
                .build();
    }
}
