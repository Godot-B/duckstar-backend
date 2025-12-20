package com.duckstar.repository.SurveyVote;

import java.util.List;

import static com.duckstar.web.dto.SurveyResponseDto.*;

public interface SurveyVoteRepositoryCustom {
    List<AnimeBallotDto> getVoteHistoryBySubmissionId(Long submissionId);
}