package com.duckstar.repository.AnimeVote;

import com.duckstar.web.dto.VoteResponseDto.AnimeBallotDto;

import java.util.List;

public interface AnimeVoteRepositoryCustom {
    List<AnimeBallotDto> getVoteHistoryBySubmissionId(Long submissionId);
}
