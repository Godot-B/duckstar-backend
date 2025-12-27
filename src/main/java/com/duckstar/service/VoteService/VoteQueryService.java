package com.duckstar.service.VoteService;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.MemberHandler;
import com.duckstar.apiPayload.exception.handler.VoteHandler;
import com.duckstar.domain.Member;
import com.duckstar.domain.Survey;
import com.duckstar.domain.enums.BallotType;
import com.duckstar.domain.enums.Gender;
import com.duckstar.domain.enums.SurveyStatus;
import com.duckstar.domain.mapping.surveyVote.SurveyVoteSubmission;
import com.duckstar.repository.SurveyCandidate.SurveyCandidateRepository;
import com.duckstar.repository.SurveyRepository;
import com.duckstar.repository.SurveyVote.SurveyVoteRepository;
import com.duckstar.repository.SurveyVoteSubmission.SurveyVoteSubmissionRepository;
import com.duckstar.security.repository.MemberRepository;
import com.duckstar.web.support.VoteCookieManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.duckstar.web.dto.SurveyResponseDto.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VoteQueryService {
    private final SurveyRepository surveyRepository;
    private final SurveyCandidateRepository surveyCandidateRepository;
    private final MemberRepository memberRepository;
    private final SurveyVoteSubmissionRepository surveyVoteSubmissionRepository;
    private final VoteCookieManager voteCookieManager;
    private final SurveyVoteRepository surveyVoteRepository;

    public AnimeCandidateListDto getAnimeCandidateListDto(Long surveyId, Long memberId) {
        // 서베이 존재, 투표 가능 검사
        Survey survey = surveyRepository.findById(surveyId).orElseThrow(() ->
                new VoteHandler(ErrorStatus.SURVEY_NOT_FOUND));

        SurveyStatus status = survey.getStatus();
        if (status != SurveyStatus.OPEN) {
            throw new VoteHandler(ErrorStatus.VOTE_CLOSED);
        }

        // 후보 조회
        List<AnimeCandidateDto> animeCandidates =
                surveyCandidateRepository.getCandidateDtosBySurveyId(survey.getId());

        // 멤버 조회
        Member member = null;
        if (memberId != null) {
            member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        }

        return AnimeCandidateListDto.builder()
                .year(survey.getYear())
                .type(survey.getSurveyType())
                .animeCandidates(animeCandidates)
                .candidatesCount(animeCandidates.size())
                .memberGender(member != null ? member.getGender() : Gender.UNKNOWN)
                .memberAgeGroup(member != null ? member.getAgeGroup() : null)
                .build();
    }

    public AnimeVoteHistoryDto getAnimeVoteHistoryDto(
            Long surveyId,
            Long memberId,
            HttpServletRequest req
    ) {
        // 서베이 존재, 투표 가능 검사
        Survey survey = surveyRepository.findById(surveyId).orElseThrow(() ->
                new VoteHandler(ErrorStatus.SURVEY_NOT_FOUND));
        if (survey.getStatus() != SurveyStatus.OPEN) {
            throw new VoteHandler(ErrorStatus.VOTE_CLOSED);
        }

        // 제출 찾기
        String cookieId = voteCookieManager.readCookie(req, survey.getSurveyType());
        String principalKey = voteCookieManager.toPrincipalKey(memberId, cookieId);
        if (principalKey == null) {
            throw new VoteHandler(ErrorStatus.VOTE_AUTH_REQUIRED);
        }
        Optional<SurveyVoteSubmission> submissionOpt =
                surveyVoteSubmissionRepository.findBySurvey_IdAndPrincipalKey(survey.getId(), principalKey);
        if (submissionOpt.isEmpty()) {
            throw new VoteHandler(ErrorStatus.NOT_VOTED_YET);
        }
        SurveyVoteSubmission submission = submissionOpt.get();

        // 투표 내역 찾기
        Long submissionId = submission.getId();
        List<AnimeBallotDto> ballotDtos =
                surveyVoteRepository.getVoteHistoryBySubmissionId(submissionId);
        int size = ballotDtos.size();
        int normalCount = (int) ballotDtos.stream()
                .filter(dto -> dto.getBallotType() == BallotType.NORMAL)
                .count();

        return AnimeVoteHistoryDto.builder()
                .memberId(memberId)
                .nickName(memberId != null ? submission.getMember().getNickname() : null)
                .submissionId(submissionId)
                .year(survey.getYear())
                .type(survey.getSurveyType())
                .normalCount(normalCount)
                .bonusCount(size - normalCount)
                .submittedAt(submission.getUpdatedAt())
                .animeBallotDtos(ballotDtos)
                .build();
    }
}
