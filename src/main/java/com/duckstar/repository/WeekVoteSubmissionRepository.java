package com.duckstar.repository;

import com.duckstar.domain.mapping.WeekVoteSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WeekVoteSubmissionRepository extends JpaRepository<WeekVoteSubmission, Long> {
    Optional<WeekVoteSubmission> findByWeekIdAndPrincipalKey(Long weekId, String principalKey);

    Optional<WeekVoteSubmission> findByPrincipalKey(String principalKey);

    Optional<WeekVoteSubmission> findByMember_Id(Long memberId);

    boolean existsByMember_Id(Long memberId);

    List<WeekVoteSubmission> findAllByMember_Id(Long memberId);
}
