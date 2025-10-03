package com.duckstar.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.MemberHandler;
import com.duckstar.apiPayload.exception.handler.VoteHandler;
import com.duckstar.domain.Member;
import com.duckstar.domain.Week;
import com.duckstar.domain.enums.BallotType;
import com.duckstar.domain.enums.Gender;
import com.duckstar.domain.enums.VoteCategory;
import com.duckstar.domain.enums.VoteStatus;
import com.duckstar.domain.mapping.AnimeCandidate;
import com.duckstar.domain.mapping.AnimeVote;
import com.duckstar.domain.mapping.WeekVoteSubmission;
import com.duckstar.repository.AnimeCandidate.AnimeCandidateRepository;
import com.duckstar.repository.AnimeVote.AnimeVoteRepository;
import com.duckstar.repository.Week.WeekRepository;
import com.duckstar.repository.WeekVoteSubmissionRepository;
import com.duckstar.security.repository.MemberRepository;
import com.duckstar.web.dto.VoteRequestDto;
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

    @Transactional
    public void voteAnime(
            AnimeVoteRequest request,
            Long memberId,
            String cookieId,
            String ipHash,
            HttpServletRequest requestRaw,
            HttpServletResponse responseRaw
    ) {
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

        boolean isConsecutive = false;
        if (member != null) {
            member.setGender(gender);
            LocalDateTime lastWeekStartedAt = ballotWeek.getStartDateTime().minusWeeks(1);
            Week lastWeek = weekService.getWeekByTime(lastWeekStartedAt);

            isConsecutive = weekVoteSubmissionRepository.existsByWeek_IdAndMember_Id(
                    lastWeek.getId(), member.getId());
        }

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
        if (member != null) member.updateStreak(isConsecutive);

        voteCookieManager.markVotedThisWeek(requestRaw, responseRaw);
    }
}
