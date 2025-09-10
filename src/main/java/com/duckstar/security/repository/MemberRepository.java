package com.duckstar.security.repository;

import com.duckstar.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    @Query("SELECT m FROM Member m WHERE m.kakaoId = :kakaoId OR m.kakaoId LIKE CONCAT(:kakaoId, '_retired_%')")
    Optional<Member> findByKakaoIdOrRetired(@Param("kakaoId") String kakaoId);
}
