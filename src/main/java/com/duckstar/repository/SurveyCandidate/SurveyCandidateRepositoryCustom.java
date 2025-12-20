package com.duckstar.repository.SurveyCandidate;

import java.util.List;

import static com.duckstar.web.dto.SurveyResponseDto.*;

public interface SurveyCandidateRepositoryCustom {
    List<AnimeCandidateDto> getCandidateDtosBySurveyId(Long surveyId);
}
