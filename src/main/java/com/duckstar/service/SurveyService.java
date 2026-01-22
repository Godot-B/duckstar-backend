package com.duckstar.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.SurveyHandler;
import com.duckstar.domain.Survey;
import com.duckstar.domain.enums.SurveyStatus;
import com.duckstar.repository.SurveyCandidate.SurveyCandidateRepository;
import com.duckstar.repository.SurveyRepository;
import com.duckstar.repository.SurveyVoteSubmission.SurveyVoteSubmissionRepository;
import com.duckstar.security.MemberPrincipal;
import com.duckstar.web.support.VoteCookieManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.duckstar.web.dto.ChartDto.*;
import static com.duckstar.web.dto.RankInfoDto.*;
import static com.duckstar.web.dto.SurveyResponseDto.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final VoteCookieManager voteCookieManager;
    private final SurveyVoteSubmissionRepository surveyVoteSubmissionRepository;
    private final SurveyCandidateRepository surveyCandidateRepository;

    @Transactional
    public void updateStatus() {
        LocalDateTime now = LocalDateTime.now();

        List<Survey> surveys = surveyRepository.findAllByStatusIn(
                List.of(SurveyStatus.NOT_YET, SurveyStatus.OPEN, SurveyStatus.CLOSED));

        surveys.forEach(survey -> survey.updateStatus(now));
    }

    public SurveyRankPage getSurveyRankPage(
            Long surveyId, MemberPrincipal principal, Pageable pageable) {
        Survey survey = surveyRepository.findById(surveyId).orElseThrow(() ->
                new SurveyHandler(ErrorStatus.SURVEY_NOT_FOUND));

        if (survey.getStatus() != SurveyStatus.RESULT_OPEN) {
            throw new SurveyHandler(ErrorStatus.SURVEY_RESULT_NOT_OPEN);
        }

        Page<SurveyRankDto> items = surveyCandidateRepository
                .getSurveyRankDtosBySurveyId(surveyId, principal, pageable);

        return SurveyRankPage.builder()
                .voteTotalCount(survey.getVotes())
                .surveyRankDtos(items.getContent())
                .page(items.getNumber())
                .size(items.getSize())
                .totalPages(items.getTotalPages())
                .totalElements(items.getTotalElements())
                .isFirst(items.isFirst())
                .isLast(items.isLast())
                .build();
    }

    public List<SurveyDto> getSurveyDtos(Long memberId, HttpServletRequest req) {
        List<Survey> surveys = surveyRepository.findAll();
        return surveys.stream()
                .map(s -> {
                    String cookieId = voteCookieManager.readCookie(req, s.getSurveyType());
                    String principalKey = voteCookieManager.toPrincipalKey(memberId, cookieId);
                    return getSurveyDto(principalKey, s);
                })
                .toList();
    }

    public SurveyDto getSurveyDto(
            Long surveyId,
            Long memberId,
            HttpServletRequest req
    ) {
        Survey survey = surveyRepository.findById(surveyId).orElseThrow(() ->
                new SurveyHandler(ErrorStatus.SURVEY_NOT_FOUND));

        String cookieId = voteCookieManager.readCookie(req, survey.getSurveyType());
        String principalKey = voteCookieManager.toPrincipalKey(memberId, cookieId);

        return getSurveyDto(principalKey, survey);
    }

    private SurveyDto getSurveyDto(String principalKey, Survey survey) {
        boolean hasVoted = principalKey != null &&
                surveyVoteSubmissionRepository
                        .existsBySurveyAndPrincipalKey(survey, principalKey);

        return SurveyDto.builder()
                .thumbnailUrl(survey.getThumbnailUrl())
                .surveyId(survey.getId())
                .hasVoted(hasVoted)
                .status(survey.getStatus())
                .year(survey.getYear())
                .type(survey.getSurveyType())
                .startDateTime(survey.getStartDateTime())
                .endDateTime(survey.getEndDateTime())
                .build();
    }
}
