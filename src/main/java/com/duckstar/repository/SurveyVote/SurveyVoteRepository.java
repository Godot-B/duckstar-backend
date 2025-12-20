package com.duckstar.repository.SurveyVote;

import com.duckstar.domain.mapping.surveyVote.SurveyVote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyVoteRepository extends JpaRepository<SurveyVote, Long>, SurveyVoteRepositoryCustom {
}