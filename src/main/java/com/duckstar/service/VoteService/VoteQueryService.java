package com.duckstar.service.VoteService;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.MemberHandler;
import com.duckstar.apiPayload.exception.handler.VoteHandler;
import com.duckstar.domain.Member;
import com.duckstar.domain.Survey;
import com.duckstar.domain.enums.Gender;
import com.duckstar.domain.enums.SurveyStatus;
import com.duckstar.repository.SurveyCandidate.SurveyCandidateRepository;
import com.duckstar.repository.SurveyRepository;
import com.duckstar.repository.SurveyVoteSubmissionRepository;
import com.duckstar.security.repository.MemberRepository;
import com.duckstar.web.dto.SurveyResponseDto;
import com.duckstar.web.support.VoteCookieManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    public List<SurveyDto> getSurveyDtos(Long memberId, HttpServletRequest req) {
        List<Survey> surveys = surveyRepository.findAll();
        return surveys.stream()
                .map(s -> getSurveyDto(memberId, req, s))
                .toList();
    }

    public SurveyDto getSurveyDto(
            Long surveyId,
            Long memberId,
            HttpServletRequest req
    ) {
        Survey survey = surveyRepository.findById(surveyId).orElseThrow(() ->
                new VoteHandler(ErrorStatus.SURVEY_NOT_FOUND));

        return getSurveyDto(memberId, req, survey);
    }

    private SurveyDto getSurveyDto(
            Long memberId,
            HttpServletRequest req,
            Survey survey
    ) {
        String cookieId = voteCookieManager.readCookie(req, survey.getSurveyType());
        String principalKey = voteCookieManager.toPrincipalKey(memberId, cookieId);

        boolean hasVoted = surveyVoteSubmissionRepository
                .existsBySurveyAndPrincipalKey(survey, principalKey);

        return SurveyDto.builder()
                .hasVoted(hasVoted)
                .status(survey.getStatus())
                .year(survey.getYear())
                .type(survey.getSurveyType())
                .startDate(survey.getStartDateTime().toLocalDate())
                .endDate(survey.getEndDateTime().toLocalDate())
                .build();
    }

    public AnimeCandidateListDto getAnimeCandidateList(Long surveyId, Long memberId) {
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
                .animeCandidates(animeCandidates)
                .candidatesCount(animeCandidates.size())
                .memberGender(member != null ? member.getGender() : Gender.UNKNOWN)
                .memberAgeGroup(member != null ? member.getAgeGroup() : null)
                .build();
    }


}
