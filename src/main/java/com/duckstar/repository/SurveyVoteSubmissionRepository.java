package com.duckstar.repository;

import com.duckstar.domain.Survey;
import com.duckstar.domain.mapping.surveyVote.SurveyVoteSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SurveyVoteSubmissionRepository extends JpaRepository<SurveyVoteSubmission, Long> {
  boolean existsBySurveyAndPrincipalKey(Survey survey, String principalKey);

  Optional<SurveyVoteSubmission> findBySurvey_IdAndPrincipalKey(Long surveyId, String principalKey);
}