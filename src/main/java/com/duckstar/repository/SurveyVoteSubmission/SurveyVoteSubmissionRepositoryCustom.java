package com.duckstar.repository.SurveyVoteSubmission;

import com.duckstar.domain.mapping.surveyVote.SurveyVoteSubmission;

import java.util.List;
import java.util.Optional;

public interface SurveyVoteSubmissionRepositoryCustom {
    Optional<SurveyVoteSubmission> findLocalSubmission(Long surveyId, String cookieId);
    Long getEligibleCountBySurveyId(Long surveyId, List<String> outlaws);
}
