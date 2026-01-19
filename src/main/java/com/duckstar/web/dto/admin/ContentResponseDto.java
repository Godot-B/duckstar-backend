package com.duckstar.web.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

import static com.duckstar.web.dto.EpisodeResponseDto.*;
import static com.duckstar.web.dto.WeekResponseDto.*;
import static com.duckstar.web.dto.admin.AdminLogDto.*;

public class ContentResponseDto {

    @Builder
    @Getter
    @AllArgsConstructor
    public static class EpisodeInfoDto {
        EpisodeDto episodeDto;

        ManagerProfileDto managerProfileDto;
    }

    @Builder
    @Getter
    public static class AdminEpisodeListDto {
        Integer episodeTotalCount;

        List<EpisodeInfoDto> episodeInfoDtos;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    public static class ScheduleInfoDto {
        String titleKor;

        String mainThumbnailUrl;

        EpisodeDto episodeDto;

        ManagerProfileDto managerProfileDto;
    }

    @Builder
    @Getter
    public static class AdminScheduleInfoDto {
        WeekDto weekDto;

        Integer animeTotalCount;

        List<ScheduleInfoDto> scheduleInfoDtos;
    }

    @Builder
    @Getter
    public static class EpisodeManageResultDto {
        EpisodeResultDto episodeResultDto;

        ManagerProfileDto managerProfileDto;
    }
}
