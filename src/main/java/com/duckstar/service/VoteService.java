package com.duckstar.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.EpisodeHandler;
import com.duckstar.apiPayload.exception.handler.MemberHandler;
import com.duckstar.apiPayload.exception.handler.VoteHandler;
import com.duckstar.domain.Member;
import com.duckstar.domain.Week;
import com.duckstar.domain.enums.*;
import com.duckstar.domain.mapping.*;
import com.duckstar.domain.mapping.legacy_vote.AnimeCandidate;
import com.duckstar.domain.mapping.legacy_vote.AnimeVote;
import com.duckstar.repository.AnimeCandidate.AnimeCandidateRepository;
import com.duckstar.repository.AnimeVote.AnimeVoteRepository;
import com.duckstar.repository.Episode.EpisodeRepository;
import com.duckstar.repository.EpisodeStar.EpisodeStarRepository;
import com.duckstar.repository.Week.WeekRepository;
import com.duckstar.repository.WeekVoteSubmission.WeekVoteSubmissionRepository;
import com.duckstar.security.repository.MemberRepository;
import com.duckstar.web.dto.VoteRequestDto.BallotRequestDto;
import com.duckstar.web.dto.VoteRequestDto.AnimeVoteRequest;
import com.duckstar.web.dto.WeekResponseDto.WeekDto;
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

import static com.duckstar.web.dto.VoteRequestDto.*;
import static com.duckstar.web.dto.VoteResponseDto.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VoteService {

    private final AnimeCandidateRepository animeCandidateRepository;
    private final WeekRepository weekRepository;
    private final AnimeVoteRepository animeVoteRepository;
    private final WeekVoteSubmissionRepository weekVoteSubmissionRepository;
    private final MemberRepository memberRepository;

    private final WeekService weekService;
    private final VoteCookieManager voteCookieManager;
    private final EpisodeRepository episodeRepository;
    private final EpisodeStarRepository episodeStarRepository;

    /**
     * 일반-보너스 투표 방식
     */
    public AnimeCandidateListDto getAnimeCandidateList(Long memberId) {
        Week currentWeek = weekService.getCurrentWeek();

        VoteStatus status = currentWeek.getStatus();
        if (status != VoteStatus.OPEN) {
            return AnimeCandidateListDto.ofEmpty(status);
        }

        List<AnimeCandidateDto> animeCandidates =
                animeCandidateRepository.getAnimeCandidateDtosByWeekId(currentWeek.getId());

        Member member = null;
        if (memberId != null) {
            member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        }

        return AnimeCandidateListDto.builder()
                .status(status)
                .weekId(currentWeek.getId())
                .weekDto(WeekDto.of(currentWeek))
                .animeCandidates(animeCandidates)
                .candidatesCount(animeCandidates.size())
                .memberGender(member != null ? member.getGender() : Gender.UNKNOWN)
                .build();
    }

    public AnimeVoteHistoryDto getAnimeVoteHistory(Long memberId, String cookieId) {
        String principalKey = voteCookieManager.toPrincipalKey(memberId, cookieId);

        if (principalKey == null) {
            return AnimeVoteHistoryDto.ofEmpty(memberId);
        }

        Week currentWeek = weekService.getCurrentWeek();

        Optional<WeekVoteSubmission> submissionOpt =
                weekVoteSubmissionRepository.findByWeek_IdAndPrincipalKey(currentWeek.getId(), principalKey);

        Optional<Long> submissionIdOpt = submissionOpt.map(WeekVoteSubmission::getId);
        if (submissionIdOpt.isEmpty()) {
            return AnimeVoteHistoryDto.ofEmpty(memberId);
        }

        Long submissionId = submissionIdOpt.get();
        WeekVoteSubmission submission = weekVoteSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new VoteHandler(ErrorStatus.NOT_VOTED_YET));

        if (!submission.getPrincipalKey().equals(principalKey)) {
            throw new VoteHandler(ErrorStatus.VOTE_HISTORY_ACCESS_DENIED);
        }

        List<AnimeBallotDto> ballotDtos = animeVoteRepository.getVoteHistoryBySubmissionId(submissionId);
        int size = ballotDtos.size();
        int normalCount = (int) ballotDtos.stream()
                .filter(dto -> dto.getBallotType() == BallotType.NORMAL)
                .count();

        return AnimeVoteHistoryDto.builder()
                .hasVoted(true)
                .memberId(memberId)
                .nickName(memberId != null ? submission.getMember().getNickname() : null)
                .submissionId(submissionId)
                .weekDto(WeekDto.of(currentWeek))
                .category(VoteCategory.ANIME)
                .normalCount(normalCount)
                .bonusCount(size - normalCount)
                .submittedAt(submission.getUpdatedAt())
                .animeBallotDtos(ballotDtos)
                .build();
    }

    @Transactional
    public void revoteAnime(
            Long submissionId,
            AnimeRevoteRequest request,
            Long memberId
    ) {
        if (memberId == null) {
            throw new VoteHandler(ErrorStatus.MEMBER_NOT_FOUND);
        }
        Member member = memberRepository.findById(memberId).orElseThrow(() ->
                new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        //=== 투표 주차 유효성 검사 ===//
        Long ballotWeekId = request.getWeekId();
        Week ballotWeek = weekRepository.findWeekById(ballotWeekId).orElseThrow(() ->
                new VoteHandler(ErrorStatus.WEEK_NOT_FOUND));
        Week currentWeek = weekService.getCurrentWeek();
        boolean isValidWeek =
                ballotWeek.getStatus() == VoteStatus.OPEN &&
                        ballotWeek.getId().equals(currentWeek.getId());  // 안전하게 PK 비교

        if (!isValidWeek) {
            throw new VoteHandler(ErrorStatus.VOTE_CLOSED);
        }

        Gender gender = request.getGender();
        member.setGender(gender);

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
                animeCandidateRepository.findValidIdsForWeek(ballotWeekId, allIds)
        );
        if (valid.size() != allIds.size()) {
            throw new VoteHandler(ErrorStatus.INVALID_CANDIDATE_INCLUDED);
        }

        //=== 삭제 ===//
        if (!removeReqIds.isEmpty()) {
            animeVoteRepository.deleteAllByWeekVoteSubmission_IdAndAnimeCandidate_IdIn(
                    submissionId, removeReqIds);
        }

        WeekVoteSubmission submission = weekVoteSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new VoteHandler(ErrorStatus.SUBMISSION_NOT_FOUND));

        //=== 수정 ===//
        if (!updateReqIds.isEmpty()) {
            List<AnimeVote> votesToUpdate = animeVoteRepository
                    .findAllByWeekVoteSubmission_IdAndAnimeCandidate_IdIn(submission.getId(), updateReqIds);

            Map<Long, BallotType> map = updateRequests.stream()
                    .collect(Collectors.toMap(
                            BallotRequestDto::getCandidateId,
                            BallotRequestDto::getBallotType
                    ));

            for (AnimeVote vote : votesToUpdate) {
                vote.updateScore(map.get(vote.getAnimeCandidate().getId()));
            }
        }

        //=== 추가 ===//
        if (!addReqIds.isEmpty()) {
            List<AnimeVote> rows = new ArrayList<>();
            for (BallotRequestDto dto : addRequests) {
                AnimeCandidate candidate =
                        animeCandidateRepository.getReferenceById(dto.getCandidateId()); // 프록시 객체 반환

                AnimeVote animeVote = AnimeVote.create(
                        submission,
                        candidate,
                        dto.getBallotType()
                );
                rows.add(animeVote);
            }
            animeVoteRepository.saveAll(rows);
        }

        submission.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * 별점 투표 방식
     */
    public StarCandidateListDto getStarCandidatesByWindow(Long memberId, String cookieId) {
        String principalKey = voteCookieManager.toPrincipalKey(memberId, cookieId);

        LocalDateTime now = LocalDateTime.now();
        Week week = weekService.getWeekByTime(now);

        Optional<WeekVoteSubmission> submissionOpt =
                weekVoteSubmissionRepository.findByWeek_IdAndPrincipalKey(
                        week.getId(), principalKey);

        Map<Long, StarInfoDto> userStarInfoMap;
        if (submissionOpt.isPresent()) {  // 투표 내역 존재
            userStarInfoMap = new HashMap<>();
            WeekVoteSubmission submission = submissionOpt.get();

            Map<Episode, Integer> episodeMap =
                    episodeStarRepository.findEpisodeMapBySubmissionId(submission.getId());

            for (Map.Entry<Episode, Integer> entry : episodeMap.entrySet()) {
                Episode episode = entry.getKey();
                userStarInfoMap.put(episode.getId(), StarInfoDto.of(entry.getValue(), episode));
            }

        } else {
            userStarInfoMap = Map.of();
        }

        List<StarCandidateDto> starCandidates =
                episodeRepository.getStarCandidatesByDuration(
                        now.minusHours(36),
                        now
                );

        starCandidates.forEach(sc -> {
            Long episodeId = sc.getEpisodeId();
            if (episodeId != null) {
                StarInfoDto info = userStarInfoMap.get(episodeId);
                sc.setUserHistory(info);
            }
        });

        return StarCandidateListDto.builder()
                .weekDto(WeekDto.of(week))
                .starCandidates(starCandidates)
                .build();
    }

    @Transactional
    public StarInfoDto voteOrUpdateStar(
            StarRequestDto request,
            Long memberId,
            String cookieId,
            String ipHash
    ) {
        //=== 투표 유효성(방영시간으로부터 36시간 이내인지) ===//
        Long episodeId = request.getEpisodeId();
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new EpisodeHandler(ErrorStatus.EPISODE_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime scheduledAt = episode.getScheduledAt();
        Boolean isBreak = episode.getIsBreak();

        // 유효 투표 조건: 방송 이후 36시간 동안
        boolean isValid = !now.isBefore(scheduledAt) && now.isBefore(scheduledAt.plusHours(36)) &&
                // 그리고 휴방 아님
                (isBreak == null || !isBreak);

        if (!isValid) {
            throw new VoteHandler(ErrorStatus.VOTE_CLOSED);
        }

        //=== 멤버 찾기 ===//
        Member member = memberId != null ?
                memberRepository.findById(memberId).orElseThrow(() ->
                        new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND)) :
                null;

                                            // ** 방영된 에피소드가 속한 주
        Week currentWeek = weekService.getWeekByTime(scheduledAt);
//        boolean isConsecutive = false;
//        if (member != null) {
//            LocalDateTime lastWeekStartedAt = currentWeek.getStartDateTime().minusWeeks(1);
//            Week lastWeek = weekService.getWeekByTime(lastWeekStartedAt);
//
//            // 멤버 연속 기록 체크
//            isConsecutive = weekVoteSubmissionRepository.existsByWeek_IdAndMember_Id(
//                    lastWeek.getId(), member.getId());
//        }

        //=== 제출 정보 찾기 ===//
        String principalKey = voteCookieManager.toPrincipalKey(memberId, cookieId);
        Optional<WeekVoteSubmission> submissionOpt =
                weekVoteSubmissionRepository.findByWeek_IdAndPrincipalKey(currentWeek.getId(), principalKey);

//        boolean isNewSubmission = submissionOpt.isEmpty();
        WeekVoteSubmission submission = submissionOpt.orElseGet(() ->
                weekVoteSubmissionRepository.save(WeekVoteSubmission.create(
                        currentWeek,
                        member,
                        cookieId,
                        ipHash,
                        voteCookieManager.toPrincipalKey(memberId, cookieId),
                        null,
                        VoteCategory.ANIME
                ))
        );

        //=== 별점 반영 ===//
        Integer starScore = request.getStarScore();

        Optional<EpisodeStar> episodeStarOpt = episodeStarRepository
                .findByEpisode_IdAndWeekVoteSubmission_Id(episodeId, submission.getId());

        if (episodeStarOpt.isPresent()) {
            episodeStarOpt.get().updateStarScore(starScore);
        } else {
            episodeStarRepository.save(
                    EpisodeStar.create(
                            submission,
                            episode,
                            starScore
                    )
            );
        }

        //=== 마무리 작업 ===//
        // 멤버 연속 기록 반영
//        if (isNewSubmission && member != null) member.updateStreak(isConsecutive);

        return StarInfoDto.of(starScore, episode);
    }
    
    @Transactional
    public void withdrawStar(
            Long episodeId,
            Long memberId,
            String cookieId
    ) {
        //=== 제출 정보 찾기 ===//
        Week currentWeek = weekService.getCurrentWeek();
        
        String principalKey = voteCookieManager.toPrincipalKey(memberId, cookieId);
        Optional<WeekVoteSubmission> submissionOpt =
                weekVoteSubmissionRepository.findByWeek_IdAndPrincipalKey(currentWeek.getId(), principalKey);

        if (submissionOpt.isEmpty()) {
            throw new VoteHandler(ErrorStatus.SUBMISSION_NOT_FOUND);
        }

        Map<Long, EpisodeStar> episodeStarMap =
                episodeStarRepository.findAllByWeekVoteSubmission_Id(submissionOpt.get().getId())
                        .stream().collect(Collectors.toMap(
                                es -> es.getEpisode().getId(),
                                es -> es
                        ));

        EpisodeStar target = episodeStarMap.get(episodeId);
        if (target == null) {
            throw new VoteHandler(ErrorStatus.STAR_NOT_FOUND);
        }
        target.withdrawScore();
    }

    @Transactional
    public void voteAnime(
            AnimeVoteRequest request,
            Long memberId,
            String cookieId,
            String ipHash,
            HttpServletRequest requestRaw,
            HttpServletResponse responseRaw
    ) {
        /**
         * request 에서 week 빼고,
         * candidate 의 week 유효성 검사만 하는 것으로 추후 수정
         */

        //=== 투표 주차 유효성 검사 ===//
        Long ballotWeekId = request.getWeekId();
        Week ballotWeek = weekRepository.findWeekById(ballotWeekId).orElseThrow(() ->
                new VoteHandler(ErrorStatus.WEEK_NOT_FOUND));
        Week currentWeek = weekService.getCurrentWeek();

        boolean isValidWeek =
                ballotWeek.getStatus() == VoteStatus.OPEN &&
                        ballotWeek.getId().equals(currentWeek.getId());  // 안전하게 PK 비교

        if (!isValidWeek) {
            throw new VoteHandler(ErrorStatus.VOTE_CLOSED);
        }

        Member member = memberId != null ?
                memberRepository.findById(memberId).orElseThrow(() ->
                        new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND)) :
                null;

        Gender gender = request.getGender();

//        boolean isConsecutive = false;
//        if (member != null) {
//            member.setGender(gender);
//            LocalDateTime lastWeekStartedAt = ballotWeek.getStartDateTime().minusWeeks(1);
//            Week lastWeek = weekService.getWeekByTime(lastWeekStartedAt);
//
//            isConsecutive = weekVoteSubmissionRepository.existsByWeek_IdAndMember_Id(
//                    lastWeek.getId(), member.getId());
//        }

        //=== 중복 투표 방지 ===//
        WeekVoteSubmission submission = WeekVoteSubmission.create(
                currentWeek,
                member,
                cookieId,
                ipHash,
                voteCookieManager.toPrincipalKey(memberId, cookieId),
                gender,
                VoteCategory.ANIME
        );
        WeekVoteSubmission savedSubmission;
        try {
            savedSubmission = weekVoteSubmissionRepository.save(submission);
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
                animeCandidateRepository.findValidIdsForWeek(ballotWeekId, candidateIds)
        );
        if (valid.size() != uniq.size()) {
            throw new VoteHandler(ErrorStatus.INVALID_CANDIDATE_INCLUDED);
        }

        //=== 저장 ===//

        List<AnimeVote> rows = new ArrayList<>();
        for (BallotRequestDto dto : ballotRequests) {
            AnimeCandidate candidate =
                    animeCandidateRepository.getReferenceById(dto.getCandidateId()); // 프록시 객체 반환

            AnimeVote animeVote = AnimeVote.create(
                    savedSubmission,
                    candidate,
                    dto.getBallotType()
            );
            rows.add(animeVote);
        }

        animeVoteRepository.saveAll(rows);
//        if (member != null) member.updateStreak(isConsecutive);

//        voteCookieManager.markVotedThisWeek(requestRaw, responseRaw);
    }
}
