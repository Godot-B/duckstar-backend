package com.duckstar.service.VoteService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static com.duckstar.web.dto.SurveyRequestDto.*;
import static com.duckstar.web.dto.VoteRequestDto.*;
import static com.duckstar.web.dto.VoteResponseDto.*;

public interface VoteCommandService {
    void voteSurvey(
            AnimeVoteRequest request,
            Long memberId,
            HttpServletRequest requestRaw,
            HttpServletResponse responseRaw
    );

    void revoteSurvey(
            Long submissionId,
            AnimeRevoteRequest request,
            Long memberId
    );

    VoteResultDto voteOrUpdateStar(
            StarRequestDto request,
            Long memberId,
            HttpServletRequest requestRaw,
            HttpServletResponse responseRaw
    );

    VoteFormResultDto voteOrUpdateStarWithLoginAndComment(
            LateStarRequestDto request,
            Long memberId,
            HttpServletRequest requestRaw
    );

    void withdrawStar(
            Long episodeId,
            Long episodeStarId,
            Long memberId,
            HttpServletRequest requestRaw,
            HttpServletResponse responseRaw
    );

    void refreshEpisodeStatsByWeekId(Long weekId);
}
