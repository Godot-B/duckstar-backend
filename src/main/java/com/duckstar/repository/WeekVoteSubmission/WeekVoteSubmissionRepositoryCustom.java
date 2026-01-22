package com.duckstar.repository.WeekVoteSubmission;

import com.duckstar.domain.mapping.weeklyVote.WeekVoteSubmission;

import java.util.List;
import java.util.Optional;

import static com.duckstar.web.dto.admin.SubmissionResponseDto.*;

public interface WeekVoteSubmissionRepositoryCustom {
    Optional<WeekVoteSubmission> findLocalSubmission(Long weekId, String cookieId);

    List<SubmissionCountDto> getSubmissionCountDtos(int offset, int limit);

    List<EpisodeStarDto> getEpisodeStarDtosByWeekIdAndIpHash(Long weekId, String ipHash);
}
