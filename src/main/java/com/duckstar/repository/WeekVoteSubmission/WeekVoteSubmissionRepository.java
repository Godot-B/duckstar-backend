package com.duckstar.repository.WeekVoteSubmission;

import com.duckstar.domain.mapping.WeekVoteSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WeekVoteSubmissionRepository extends JpaRepository<WeekVoteSubmission, Long>, WeekVoteSubmissionRepositoryCustom {
    Optional<WeekVoteSubmission> findByWeek_IdAndPrincipalKey(Long weekId, String principalKey);
    Optional<WeekVoteSubmission> findByWeek_IdAndMember_Id(Long weekIdm, Long memberId);
    Optional<WeekVoteSubmission> findByWeek_IdAndCookieId(Long weekId, String cookieId);

    List<WeekVoteSubmission> findAllByMember_Id(Long memberId);

    List<WeekVoteSubmission> findAllByWeek_Id(Long weekId);
}
