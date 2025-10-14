package com.duckstar.repository.WeekVoteSubmission;

public interface WeekVoteSubmissionRepositoryCustom {

    boolean existsByWeek_IdAndMember_Id(Long weekId, Long memberId);
}
