package com.duckstar.repository.SurveyVoteSubmission;

import com.duckstar.domain.mapping.surveyVote.SurveyVoteSubmission;

import java.util.Optional;

public interface SurveyVoteSubmissionRepositoryCustom {
    Optional<SurveyVoteSubmission> findLocalSubmission(Long surveyId, String cookieId);
}
