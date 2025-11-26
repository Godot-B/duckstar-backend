package com.duckstar.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static com.duckstar.web.dto.VoteRequestDto.*;
import static com.duckstar.web.dto.VoteResponseDto.*;

public interface VoteCommandService {
    VoteResultDto voteOrUpdate(
            StarRequestDto request,
            Long memberId,
            HttpServletRequest requestRaw,
            HttpServletResponse responseRaw
    );

    VoteFormResultDto voteOrUpdateWithLoginAndComment(
            LateStarRequestDto request,
            Long memberId,
            HttpServletRequest requestRaw
    );

    void withdrawVote(
            Long episodeId,
            Long episodeStarId,
            Long memberId,
            HttpServletRequest requestRaw,
            HttpServletResponse responseRaw
    );

    void refreshEpisodeStatsByWeekId(Long weekId);
}
