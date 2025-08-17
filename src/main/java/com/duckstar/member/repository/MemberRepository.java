package com.duckstar.member.repository;

import com.duckstar.member.domain.Member;
import com.duckstar.member.domain.OauthId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByOauthId(OauthId oauthId);
}
