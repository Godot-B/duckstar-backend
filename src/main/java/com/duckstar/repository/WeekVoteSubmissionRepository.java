package com.duckstar.repository;

import com.duckstar.domain.mapping.WeekVoteSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WeekVoteSubmissionRepository extends JpaRepository<WeekVoteSubmission, Long> {
    Optional<WeekVoteSubmission> findByWeekIdAndPrincipalKey(Long weekId, String principalKey);
}
