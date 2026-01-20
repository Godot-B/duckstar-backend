package com.duckstar.service.EpisodeService;

import static com.duckstar.web.dto.admin.ContentResponseDto.*;
import static com.duckstar.web.dto.admin.EpisodeRequestDto.*;

public interface EpisodeCommandService {
    void updateAllEpisodeStates();

    EpisodeManageResultDto modifyEpisode(Long memberId, Long episodeId, ModifyRequestDto request);

    EpisodeManageResultDto breakEpisode(Long memberId, Long episodeId);

    EpisodeManageResultDto deleteEpisode(Long memberId, Long episodeId);

    EpisodeManageResultDto queueEpisode(Long memberId, Long animeId);
}
