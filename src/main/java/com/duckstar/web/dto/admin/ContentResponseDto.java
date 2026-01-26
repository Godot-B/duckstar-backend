package com.duckstar.web.dto.admin;

import com.duckstar.domain.Member;
import com.duckstar.domain.enums.AnimeStatus;
import com.duckstar.domain.enums.DayOfWeekShort;
import com.duckstar.domain.mapping.AdminActionLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;
import java.util.List;

import static com.duckstar.web.dto.EpisodeResponseDto.*;
import static com.duckstar.web.dto.WeekResponseDto.*;
import static com.duckstar.web.dto.admin.AdminLogDto.*;

public class ContentResponseDto {

    @Builder
    @Getter
    @AllArgsConstructor
    public static class AdminAnimeDto {
        Long animeId;

        String titleKor;
        String corp;
        String mainThumbnailUrl;

        AnimeStatus status;

        DayOfWeekShort dayOfWeek;
        LocalTime airTime;

        Integer totalEpisodes;

        ManagerProfileDto managerProfileDto;
    }

    @Builder
    @Getter
    public static class AdminAnimeListDto {
        List<AdminAnimeDto> adminAnimeDtos;

        // offset 페이징
        private Integer page;          // 요청한 페이지 번호
        private Integer size;          // 요청한 페이지당 사이즈
        private Integer totalPages;    // 전체 페이지 수
        private Long totalElements;    // 전체 데이터 수
        private Boolean isFirst;       // 첫 페이지 여부
        private Boolean isLast;        // 마지막 페이지 여부
    }

    @Builder
    @Getter
    @AllArgsConstructor
    public static class AdminEpisodeDto {
        EpisodeDto episodeDto;

        ManagerProfileDto managerProfileDto;
    }

    @Builder
    @Getter
    public static class AdminEpisodeListDto {
        Integer episodeTotalCount;

        List<AdminEpisodeDto> adminEpisodeDtos;
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

        public static EpisodeManageResultDto toResultDto(
                List<EpisodeDto> added,
                List<EpisodeDto> deleted,
                Member member,
                AdminActionLog adminActionLog
        ) {
            return EpisodeManageResultDto.builder()
                    .episodeResultDto(
                            EpisodeResultDto.builder()
                                    .addedEpisodes(added)
                                    .deletedEpisodes(deleted)
                                    .build()
                    )
                    .managerProfileDto(
                            ManagerProfileDto.of(member, adminActionLog)
                    )
                    .build();
        }
    }
}
