package com.duckstar.service.EpisodeService;

import com.duckstar.web.dto.admin.ContentResponseDto;
import com.duckstar.web.dto.admin.ContentResponseDto.AdminEpisodeListDto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

import static com.duckstar.web.dto.VoteResponseDto.*;
import static com.duckstar.web.dto.admin.ContentResponseDto.*;

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

    AdminEpisodeListDto getAdminEpisodesByAnimeId(Long animeId);

    AdminScheduleInfoDto getAdminScheduleByWeekId(Long weekId);
}
