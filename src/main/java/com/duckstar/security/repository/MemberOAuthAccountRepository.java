package com.duckstar.security.repository;

import com.duckstar.security.domain.MemberOAuthAccount;
import com.duckstar.security.domain.enums.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberOAuthAccountRepository extends JpaRepository<MemberOAuthAccount, Long> {
    void deleteAllByMemberId(Long memberId);
    Optional<MemberOAuthAccount> findByProviderAndProviderUserId(OAuthProvider oAuthProvider, String providerUserId);
    Optional<MemberOAuthAccount> findByProviderAndMemberId(OAuthProvider oAuthProvider, Long memberId);
}
