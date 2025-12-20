package com.duckstar.repository;

import com.duckstar.domain.Survey;
import com.duckstar.domain.mapping.surveyVote.SurveyVoteSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurveyVoteSubmissionRepository extends JpaRepository<SurveyVoteSubmission, Long> {
  boolean existsBySurveyAndPrincipalKey(Survey survey, String principalKey);
}