package com.duckstar.repository.SurveyVote;

import com.duckstar.domain.mapping.surveyVote.SurveyVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SurveyVoteRepository extends JpaRepository<SurveyVote, Long>, SurveyVoteRepositoryCustom {
    void deleteAllBySurveyVoteSubmission_IdAndSurveyCandidate_IdIn(Long surveyVoteSubmissionId, Collection<Long> surveyCandidateIds);
    List<SurveyVote> findAllBySurveyVoteSubmission_IdAndSurveyCandidate_IdIn(Long surveyVoteSubmissionId, Collection<Long> surveyCandidateIds);
}