package com.duckstar.service.EpisodeService;

import java.time.LocalDateTime;
import java.util.List;

import static com.duckstar.web.dto.admin.AdminLogDto.*;
import static com.duckstar.web.dto.admin.ContentResponseDto.*;
import static com.duckstar.web.dto.admin.EpisodeRequestDto.*;

public interface EpisodeCommandService {
    void updateAllEpisodeStates();

    List<ManagerProfileDto> modifyEpisode(Long memberId, Long episodeId, ModifyRequestDto request);

    EpisodeManageResultDto breakEpisode(Long memberId, Long episodeId);

    ManagerProfileDto deleteMoreThanNextWeekEpisode(Long memberId, Long episodeId, LocalDateTime baseTime);

    EpisodeManageResultDto queueEpisode(Long memberId, Long animeId);
}
