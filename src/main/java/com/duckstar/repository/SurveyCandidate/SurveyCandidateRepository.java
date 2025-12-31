package com.duckstar.repository.SurveyCandidate;

import com.duckstar.domain.mapping.surveyVote.SurveyCandidate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurveyCandidateRepository extends JpaRepository<SurveyCandidate, Long>, SurveyCandidateRepositoryCustom {
    List<SurveyCandidate> findAllBySurvey_Id(Long surveyId);
    List<SurveyCandidate> findAllBySurvey_IdAndQuarter_Id(Long surveyId, Long quarterId);
}