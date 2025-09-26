package com.duckstar.security.repository;

import com.duckstar.domain.Member;
import com.duckstar.security.domain.enums.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByProviderAndProviderId(OAuthProvider provider, String providerId);
}
