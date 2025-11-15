package com.duckstar.repository.WeekVoteSubmission;

import com.duckstar.domain.mapping.WeekVoteSubmission;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static com.duckstar.web.dto.admin.SubmissionResponseDto.*;

public interface WeekVoteSubmissionRepositoryCustom {
    Optional<WeekVoteSubmission> findLocalSubmission(Long weekId, String cookieId);

    List<SubmissionCountDto> getSubmissionCountDtos(Pageable pageable);

    List<EpisodeStarDto> getEpisodeStarDtosByWeekIdAndIpHash(Long weekId, String ipHash);

    boolean existsByWeek_IdAndMember_Id(Long weekId, Long memberId);
}
