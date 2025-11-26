package com.duckstar.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.*;
import com.duckstar.domain.Member;
import com.duckstar.domain.Quarter;
import com.duckstar.domain.Week;
import com.duckstar.domain.enums.*;
import com.duckstar.domain.mapping.*;
import com.duckstar.domain.mapping.comment.AnimeComment;
import com.duckstar.repository.AnimeComment.AnimeCommentRepository;
import com.duckstar.repository.Episode.EpisodeRepository;
import com.duckstar.repository.EpisodeStar.EpisodeStarRepository;
import com.duckstar.repository.Week.WeekRepository;
import com.duckstar.repository.WeekVoteSubmission.WeekVoteSubmissionRepository;
import com.duckstar.security.repository.MemberRepository;
import com.duckstar.security.service.ShadowBanService;
import com.duckstar.web.support.Hasher;
import com.duckstar.web.support.IdentifierExtractor;
import com.duckstar.web.support.VoteCookieManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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

    /**
     * 일반-보너스 투표 방식
     */
//    public AnimeCandidateListDto getAnimeCandidateList(Long memberId) {
//        Week currentWeek = weekService.getCurrentWeek();
//
//        VoteStatus status = currentWeek.getStatus();
//        if (status != VoteStatus.OPEN) {
//            return AnimeCandidateListDto.ofEmpty(status);
//        }
//
//        List<AnimeCandidateDto> animeCandidates =
//                animeCandidateRepository.getAnimeCandidateDtosByWeekId(currentWeek.getId());
//
//        Member member = null;
//        if (memberId != null) {
//            member = memberRepository.findById(memberId)
//                    .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
//        }
//
//        return AnimeCandidateListDto.builder()
//                .status(status)
//                .weekId(currentWeek.getId())
//                .weekDto(WeekDto.of(currentWeek))
//                .animeCandidates(animeCandidates)
//                .candidatesCount(animeCandidates.size())
//                .memberGender(member != null ? member.getGender() : Gender.UNKNOWN)
//                .build();
//    }
//
//    @Transactional
//    public void voteAnime(
//            AnimeVoteRequest request,
//            Long memberId,
//            String cookieId,
//            String ipHash,
//            HttpServletRequest requestRaw,
//            HttpServletResponse responseRaw
//    ) {
//        /**
//         * request 에서 week 빼고,
//         * candidate 의 week 유효성 검사만 하는 것으로 추후 수정
//         */
//
//        //=== 투표 주차 유효성 검사 ===//
//        Long ballotWeekId = request.getWeekId();
//        Week ballotWeek = weekRepository.findWeekById(ballotWeekId).orElseThrow(() ->
//                new VoteHandler(ErrorStatus.WEEK_NOT_FOUND));
//        Week currentWeek = weekService.getCurrentWeek();
//
//        boolean isValidWeek =
//                ballotWeek.getStatus() == VoteStatus.OPEN &&
//                        ballotWeek.getId().equals(currentWeek.getId());  // 안전하게 PK 비교
//
//        if (!isValidWeek) {
//            throw new VoteHandler(ErrorStatus.VOTE_CLOSED);
//        }
//
//        Member member = memberId != null ?
//                memberRepository.findById(memberId).orElseThrow(() ->
//                        new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND)) :
//                null;
//
//        Gender gender = request.getGender();
//
//        boolean isConsecutive = false;
//        if (member != null) {
//            member.setGender(gender);
//            LocalDateTime lastWeekStartedAt = ballotWeek.getStartDateTime().minusWeeks(1);
//            Week lastWeek = weekService.getWeekByTime(lastWeekStartedAt);
//
//            isConsecutive = weekVoteSubmissionRepository.existsByWeek_IdAndMember_Id(
//                    lastWeek.getId(), member.getId());
//        }
//
//        //=== 중복 투표 방지 ===//
//        WeekVoteSubmission submission = WeekVoteSubmission.create(
//                currentWeek,
//                member,
//                cookieId,
//                ipHash,
//                voteCookieManager.toPrincipalKey(memberId, cookieId),
//                gender,
//                VoteCategory.ANIME
//        );
//        WeekVoteSubmission savedSubmission;
//        try {
//            savedSubmission = weekVoteSubmissionRepository.save(submission);
//        } catch (DataIntegrityViolationException e) {
//            throw new VoteHandler(ErrorStatus.ALREADY_VOTED);
//        }
//
//        //=== 실제 투표지 검사: 후보 유효성(중복 포함됨, 이번 주 후보 아님) ===//
//        List<BallotRequestDto> ballotRequests = request.getBallotRequests();
//
//        List<Long> candidateIds = ballotRequests.stream()
//                .map(BallotRequestDto::getCandidateId)
//                .toList();
//
//        Set<Long> uniq = new HashSet<>(candidateIds);
//        if (uniq.size() != candidateIds.size()) {
//            throw new VoteHandler(ErrorStatus.DUPLICATE_CANDIDATE_INCLUDED);
//        }
//
//        Set<Long> valid = new HashSet<>(
//                animeCandidateRepository.findValidIdsForWeek(ballotWeekId, candidateIds)
//        );
//        if (valid.size() != uniq.size()) {
//            throw new VoteHandler(ErrorStatus.INVALID_CANDIDATE_INCLUDED);
//        }
//
//        //=== 저장 ===//
//
//        List<AnimeVote> rows = new ArrayList<>();
//        for (BallotRequestDto dto : ballotRequests) {
//            AnimeCandidate candidate =
//                    animeCandidateRepository.getReferenceById(dto.getCandidateId()); // 프록시 객체 반환
//
//            AnimeVote animeVote = AnimeVote.create(
//                    savedSubmission,
//                    candidate,
//                    dto.getBallotType()
//            );
//            rows.add(animeVote);
//        }
//
//        animeVoteRepository.saveAll(rows);
//        if (member != null) member.updateStreak(isConsecutive);
//
//        voteCookieManager.markVotedThisWeek(requestRaw, responseRaw);
//    }
//
//    public AnimeVoteHistoryDto getAnimeVoteHistory(Long memberId, String cookieId) {
//        String principalKey = voteCookieManager.toPrincipalKey(memberId, cookieId);
//
//        if (principalKey == null) {
//            return AnimeVoteHistoryDto.ofEmpty(memberId);
//        }
//
//        Week currentWeek = weekService.getCurrentWeek();
//
//        Optional<WeekVoteSubmission> submissionOpt =
//                weekVoteSubmissionRepository.findByWeek_IdAndPrincipalKey(currentWeek.getId(), principalKey);
//
//        Optional<Long> submissionIdOpt = submissionOpt.map(WeekVoteSubmission::getId);
//        if (submissionIdOpt.isEmpty()) {
//            return AnimeVoteHistoryDto.ofEmpty(memberId);
//        }
//
//        Long submissionId = submissionIdOpt.get();
//        WeekVoteSubmission submission = weekVoteSubmissionRepository.findById(submissionId)
//                .orElseThrow(() -> new VoteHandler(ErrorStatus.NOT_VOTED_YET));
//
//        if (!submission.getPrincipalKey().equals(principalKey)) {
//            throw new VoteHandler(ErrorStatus.VOTE_HISTORY_ACCESS_DENIED);
//        }
//
//        List<AnimeBallotDto> ballotDtos = animeVoteRepository.getVoteHistoryBySubmissionId(submissionId);
//        int size = ballotDtos.size();
//        int normalCount = (int) ballotDtos.stream()
//                .filter(dto -> dto.getBallotType() == BallotType.NORMAL)
//                .count();
//
//        return AnimeVoteHistoryDto.builder()
//                .hasVoted(true)
//                .memberId(memberId)
//                .nickName(memberId != null ? submission.getMember().getNickname() : null)
//                .submissionId(submissionId)
//                .weekDto(WeekDto.of(currentWeek))
//                .category(VoteCategory.ANIME)
//                .normalCount(normalCount)
//                .bonusCount(size - normalCount)
//                .submittedAt(submission.getUpdatedAt())
//                .animeBallotDtos(ballotDtos)
//                .build();
//    }
//
//    @Transactional
//    public void revoteAnime(
//            Long submissionId,
//            AnimeRevoteRequest request,
//            Long memberId
//    ) {
//        if (memberId == null) {
//            throw new VoteHandler(ErrorStatus.MEMBER_NOT_FOUND);
//        }
//        Member member = memberRepository.findById(memberId).orElseThrow(() ->
//                new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
//
//        //=== 투표 주차 유효성 검사 ===//
//        Long ballotWeekId = request.getWeekId();
//        Week ballotWeek = weekRepository.findWeekById(ballotWeekId).orElseThrow(() ->
//                new VoteHandler(ErrorStatus.WEEK_NOT_FOUND));
//        Week currentWeek = weekService.getCurrentWeek();
//        boolean isValidWeek =
//                ballotWeek.getStatus() == VoteStatus.OPEN &&
//                        ballotWeek.getId().equals(currentWeek.getId());  // 안전하게 PK 비교
//
//        if (!isValidWeek) {
//            throw new VoteHandler(ErrorStatus.VOTE_CLOSED);
//        }
//
//        Gender gender = request.getGender();
//        member.setGender(gender);
//
//        //=== 실제 투표지 검사: 후보 유효성(중복 포함됨, 이번 주 후보 아님) ===//
//        List<BallotRequestDto> addRequests = request.getAdded();
//        List<Long> addReqIds = List.of();
//        if (addRequests != null && !addRequests.isEmpty()) {
//            addReqIds = addRequests.stream()
//                    .map(BallotRequestDto::getCandidateId)
//                    .toList();
//        }
//        List<BallotRequestDto> updateRequests = request.getUpdated();
//        List<Long> updateReqIds = List.of();
//        if (updateRequests != null && !updateRequests.isEmpty()) {
//            updateReqIds = updateRequests.stream()
//                    .map(BallotRequestDto::getCandidateId)
//                    .toList();
//        }
//        List<BallotRequestDto> removed = request.getRemoved();
//        List<Long> removeReqIds = List.of();
//        if (removed != null && !removed.isEmpty()) {
//            removeReqIds = request.getRemoved().stream()
//                    .map(BallotRequestDto::getCandidateId)
//                    .toList();
//        }
//
//        List<Long> allIds = Stream.of(addReqIds, removeReqIds, updateReqIds)
//                .flatMap(List::stream)
//                .toList();
//
//        if (allIds.size() != new HashSet<>(allIds).size()) {
//            throw new VoteHandler(ErrorStatus.DUPLICATE_CANDIDATE_INCLUDED);
//        }
//
//        Set<Long> valid = new HashSet<>(
//                animeCandidateRepository.findValidIdsForWeek(ballotWeekId, allIds)
//        );
//        if (valid.size() != allIds.size()) {
//            throw new VoteHandler(ErrorStatus.INVALID_CANDIDATE_INCLUDED);
//        }
//
//        //=== 삭제 ===//
//        if (!removeReqIds.isEmpty()) {
//            animeVoteRepository.deleteAllByWeekVoteSubmission_IdAndAnimeCandidate_IdIn(
//                    submissionId, removeReqIds);
//        }
//
//        WeekVoteSubmission submission = weekVoteSubmissionRepository.findById(submissionId)
//                .orElseThrow(() -> new VoteHandler(ErrorStatus.SUBMISSION_NOT_FOUND));
//
//        //=== 수정 ===//
//        if (!updateReqIds.isEmpty()) {
//            List<AnimeVote> votesToUpdate = animeVoteRepository
//                    .findAllByWeekVoteSubmission_IdAndAnimeCandidate_IdIn(submission.getId(), updateReqIds);
//
//            Map<Long, BallotType> map = updateRequests.stream()
//                    .collect(Collectors.toMap(
//                            BallotRequestDto::getCandidateId,
//                            BallotRequestDto::getBallotType
//                    ));
//
//            for (AnimeVote vote : votesToUpdate) {
//                vote.updateScore(map.get(vote.getAnimeCandidate().getId()));
//            }
//        }
//
//        //=== 추가 ===//
//        if (!addReqIds.isEmpty()) {
//            List<AnimeVote> rows = new ArrayList<>();
//            for (BallotRequestDto dto : addRequests) {
//                AnimeCandidate candidate =
//                        animeCandidateRepository.getReferenceById(dto.getCandidateId()); // 프록시 객체 반환
//
//                AnimeVote animeVote = AnimeVote.create(
//                        submission,
//                        candidate,
//                        dto.getBallotType()
//                );
//                rows.add(animeVote);
//            }
//            animeVoteRepository.saveAll(rows);
//        }
//
//        submission.setUpdatedAt(LocalDateTime.now());
//    }
    @Override
    public VoteResultDto voteOrUpdate(
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
                    null,
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
    public VoteFormResultDto voteOrUpdateWithLoginAndComment(
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
                animeCommentRepository.findByEpisodeStar_Id(episodeStarId);
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
    public void withdrawVote(
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
                .filter(e -> e.getIsBreak() == null || !e.getIsBreak())
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
}
