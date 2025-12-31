package com.duckstar.repository.SurveyVote;

import java.util.List;
import java.util.Map;

import static com.duckstar.service.ChartService.*;
import static com.duckstar.web.dto.SurveyResponseDto.*;

public interface SurveyVoteRepositoryCustom {
    Map<Long, SurveyStatRecord> getEligibleStatMapBySurveyId(Long surveyId, List<String> outlaws);
    List<AnimeBallotDto> getVoteHistoryBySubmissionId(Long submissionId);
}