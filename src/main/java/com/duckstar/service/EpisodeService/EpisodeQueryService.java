package com.duckstar.service.EpisodeService;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

import static com.duckstar.web.dto.VoteResponseDto.*;

public interface EpisodeQueryService {
    LiveCandidateListDto getLiveCandidatesByWindow(
            Long memberId,
            HttpServletRequest requestRaw
    );

    List<WeekCandidateDto> getWeekCandidatesByYQW(
            Integer year,
            Integer quarter,
            Integer week,
            Long memberId,
            HttpServletRequest requestRaw
    );

    CandidateFormDto getCandidateForm(
            Long episodeId,
            Long memberId,
            HttpServletRequest requestRaw
    );
}
