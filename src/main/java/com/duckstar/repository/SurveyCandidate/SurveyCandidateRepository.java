package com.duckstar.repository.SurveyCandidate;

import com.duckstar.domain.mapping.surveyVote.SurveyCandidate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyCandidateRepository extends JpaRepository<SurveyCandidate, Long>, SurveyCandidateRepositoryCustom {
}