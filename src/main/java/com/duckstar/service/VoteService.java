package com.duckstar.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
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
import com.duckstar.web.dto.VoteRequestDto.AnimeBallotDto;
import com.duckstar.web.dto.VoteRequestDto.AnimeVoteRequest;
import com.duckstar.web.dto.VoteResponseDto;
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
    private final MemberService memberService;

    public AnimeCandidateListDto getAnimeCandidateList() {
        Week currentWeek = weekService.getCurrentWeek();

        List<AnimeCandidateDto> animeCandidates =
                animeCandidateRepository.getAnimeCandidateDtosByWeekId(currentWeek.getId());

        return AnimeCandidateListDto.builder()
                .weekId(currentWeek.getId())
                .weekDto(WeekDto.from(currentWeek))
                .animeCandidates(
                        animeCandidates
                )
                .candidatesCount(animeCandidates.size())
                .build();
    }

    public VoteCheckDto checkVoted(String principalKey) {
        Week currentWeek = weekService.getCurrentWeek();

        Optional<WeekVoteSubmission> submissionOpt =
                weekVoteSubmissionRepository.findByWeekIdAndPrincipalKey(currentWeek.getId(), principalKey);

        Long submissionId = submissionOpt.map(WeekVoteSubmission::getId)
                .orElse(null);

        return VoteCheckDto.of(submissionId);
    }

    public AnimeVoteHistoryDto getAnimeVoteHistory(Long submissionId, String principalKey) {
        Week currentWeek = weekService.getCurrentWeek();

        WeekVoteSubmission submission = weekVoteSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new VoteHandler(ErrorStatus.NOT_VOTED_YET));

        if (!submission.getPrincipalKey().equals(principalKey)) {
            throw new VoteHandler(ErrorStatus.VOTE_HISTORY_ACCESS_DENIED);
        }

        List<VoteResponseDto.AnimeBallotDto> ballotDtos = animeVoteRepository.getVoteHistoryBySubmissionId(submissionId);
        int size = ballotDtos.size();
        int normalCount = (int) ballotDtos.stream().filter(dto -> dto.getBallotType() == BallotType.NORMAL).count();

        return AnimeVoteHistoryDto.builder()
                .submissionId(submissionId)
                .weekDto(WeekDto.from(currentWeek))
                .category(VoteCategory.ANIME)
                .normalCount(normalCount)
                .bonusCount(size - normalCount)
                .submittedAt(submission.getCreatedAt())
                .animeBallotDtos(ballotDtos)
                .build();
    }

    @Transactional
    public VoteReceiptDto voteAnime(
            AnimeVoteRequest request,
            Long memberId,
            String cookieId,
            String principalKey
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
                memberService.findByIdOrThrow(memberId) :
                null;

        Gender gender = request.getGender();

        //=== 중복 투표 방지 ===//
        WeekVoteSubmission submission = WeekVoteSubmission.create(
                currentWeek,
                member,
                cookieId,
                principalKey,
                gender,
                VoteCategory.ANIME
        );

        try {
            weekVoteSubmissionRepository.save(submission);
        } catch (DataIntegrityViolationException e) {
            throw new VoteHandler(ErrorStatus.ALREADY_VOTED);
        }

        //=== 실제 투표지 검사: 후보 유효성(중복 포함됨, 이번 주 후보 아님) ===//
        List<AnimeBallotDto> ballotDtos = request.getBallotDtos();

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

        int normalCount = (int) ballotDtos.stream()
                .filter(dto -> dto.getBallotType() == BallotType.NORMAL)
                .count();

        //=== 저장 ===//

        List<AnimeVote> rows = new ArrayList<>();
        for (AnimeBallotDto dto : ballotDtos) {
            AnimeCandidate candidate =
                    animeCandidateRepository.getReferenceById(dto.getAnimeCandidateId()); // 프록시 객체 반환

            AnimeVote animeVote = AnimeVote.create(
                    submission,
                    candidate,
                    dto.getBallotType(),
                    gender
            );
            rows.add(animeVote);
        }

        animeVoteRepository.saveAll(rows);

        return VoteReceiptDto.builder()
                .submissionId(submission.getId())
                .weekDto(WeekDto.from(ballotWeek))
                .category(submission.getCategory())
                .normalCount(normalCount)
                .bonusCount(ballotDtos.size() - normalCount)
                .submittedAt(submission.getCreatedAt())
                .build();
    }
}
