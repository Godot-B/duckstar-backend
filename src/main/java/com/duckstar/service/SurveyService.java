package com.duckstar.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.VoteHandler;
import com.duckstar.domain.Survey;
import com.duckstar.domain.enums.SurveyStatus;
import com.duckstar.repository.SurveyRepository;
import com.duckstar.repository.SurveyVoteSubmission.SurveyVoteSubmissionRepository;
import com.duckstar.web.dto.SurveyResponseDto;
import com.duckstar.web.support.VoteCookieManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.duckstar.web.dto.SurveyResponseDto.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final VoteCookieManager voteCookieManager;
    private final SurveyVoteSubmissionRepository surveyVoteSubmissionRepository;

    @Transactional
    public void updateStatus() {
        LocalDateTime now = LocalDateTime.now();

        List<Survey> surveys = surveyRepository.findAllByStatusIn(
                List.of(SurveyStatus.NOT_YET,SurveyStatus.OPEN));

        surveys.forEach(survey -> survey.updateStatus(now));
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
                new VoteHandler(ErrorStatus.SURVEY_NOT_FOUND));

        String cookieId = voteCookieManager.readCookie(req, survey.getSurveyType());
        String principalKey = voteCookieManager.toPrincipalKey(memberId, cookieId);

        return getSurveyDto(principalKey, survey);
    }

    private SurveyDto getSurveyDto(String principalKey, Survey survey) {
        boolean hasVoted = principalKey != null &&
                surveyVoteSubmissionRepository
                        .existsBySurveyAndPrincipalKey(survey, principalKey);

        return SurveyDto.builder()
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
