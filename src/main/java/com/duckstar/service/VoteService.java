package com.duckstar.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.VoteHandler;
import com.duckstar.member.domain.Member;
import com.duckstar.domain.Week;
import com.duckstar.domain.enums.BallotType;
import com.duckstar.domain.enums.VoteCategory;
import com.duckstar.domain.enums.VoteStatus;
import com.duckstar.domain.mapping.AnimeCandidate;
import com.duckstar.domain.mapping.AnimeVote;
import com.duckstar.domain.mapping.WeekVoteSubmission;
import com.duckstar.repository.AnimeCandidate.AnimeCandidateRepository;
import com.duckstar.repository.AnimeVote.AnimeVoteRepository;
import com.duckstar.repository.Week.WeekRepository;
import com.duckstar.repository.WeekVoteSubmissionRepository;
import com.duckstar.web.dto.VoteRequestDto.AnimeBallotDto;
import com.duckstar.web.dto.VoteRequestDto.AnimeVoteRequest;
import com.duckstar.web.dto.WeekResponseDto.WeekDto;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.duckstar.web.dto.VoteResponseDto.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VoteService {

    private final WeekService weekService;
    private final AnimeCandidateRepository animeCandidateRepository;
    private final WeekRepository weekRepository;
    private final AnimeVoteRepository animeVoteRepository;
    private final WeekVoteSubmissionRepository weekVoteSubmissionRepository;

    public AnimeCandidateListDto getAnimeCandidateList() {
        Week currentWeek = weekService.getCurrentWeek();

        return AnimeCandidateListDto.builder()
                .weekId(currentWeek.getId())
                .weekDto(WeekDto.from(currentWeek))
                .animeCandidates(
                        animeCandidateRepository.getAnimeCandidateDtosByWeekId(currentWeek.getId())
                )
                .build();
    }

    public AnimeVoteHistoryDto getAnimeVoteHistory(String principalKey) {
        Week currentWeek = weekService.getCurrentWeek();

        WeekVoteSubmission submission =
                weekVoteSubmissionRepository.findByWeekIdAndPrincipalKey(currentWeek.getId(), principalKey)
                        .orElseThrow(() -> new VoteHandler(ErrorStatus.NOT_VOTED_YET));

        Long submissionId = submission.getId();

        return AnimeVoteHistoryDto.builder()
                .submissionId(submissionId)
                .weekDto(WeekDto.from(currentWeek))
                .category(VoteCategory.ANIME)
                .submittedAt(submission.getCreatedAt())
                .animeBallotDtos(
                        animeVoteRepository.getVoteHistoryBySubmissionId(submissionId)
                )
                .build();
    }

    @Transactional
    public VoteReceiptDto voteAnime(
            AnimeVoteRequest request,
            Member member,
            String cookieId,
            String principalKey
    ) {
        // 투표 주차 유효성 검사
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

        // 중복 투표 방지
        WeekVoteSubmission submission = WeekVoteSubmission.create(
                currentWeek,
                member,
                cookieId,
                principalKey,
                VoteCategory.ANIME
        );

        try {
            weekVoteSubmissionRepository.save(submission);
        } catch (DataIntegrityViolationException e) {
            throw new VoteHandler(ErrorStatus.ALREADY_VOTED);
        }

        // 실제 투표지 검사: 후보 유효성(중복 포함됨, 이번 주 후보 아님)
        List<AnimeBallotDto> ballotDtos = request.getBallotDtos();
        if (ballotDtos == null || ballotDtos.isEmpty()) {
            throw new VoteHandler(ErrorStatus.EMPTY_BALLOTS);
        }

        List<Long> candidateIds = ballotDtos.stream()
                .map(AnimeBallotDto::getAnimeCandidateId)
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

        // 투표 제한 초과 여부 검사
        int normalCount = (int) ballotDtos.stream()
                .filter(dto -> dto.getBallotType() == BallotType.NORMAL)
                .count();
        if (normalCount > 30) {
            throw new VoteHandler(ErrorStatus.VOTE_LIMIT_SURPASSED);
        }

        // 저장
        List<AnimeVote> rows = new ArrayList<>();
        for (AnimeBallotDto dto : ballotDtos) {
            AnimeCandidate candidate =
                    animeCandidateRepository.getReferenceById(dto.getAnimeCandidateId()); // 프록시 객체 반환

            AnimeVote animeVote = AnimeVote.create(
                    submission,
                    candidate,
                    dto.getBallotType()
            );
            rows.add(animeVote);
        }

        animeVoteRepository.saveAll(rows);

        return VoteReceiptDto.builder()
                .submissionId(submission.getId())
                .weekDto(WeekDto.from(ballotWeek))
                .normalCount(normalCount)
                .bonusCount(ballotDtos.size() - normalCount)
                .submittedAt(submission.getCreatedAt())
                .build();
    }
}
