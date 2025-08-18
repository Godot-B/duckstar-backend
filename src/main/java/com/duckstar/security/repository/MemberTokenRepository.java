package com.duckstar.security.repository;

import com.duckstar.security.domain.MemberToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MemberTokenRepository extends JpaRepository<MemberToken, Long> {
    Optional<MemberToken> findByRefreshToken(String refreshToken);
    void deleteAllByMemberId(Long memberId);

    Optional<MemberToken> findByRefreshTokenAndIsValidFalseAndRefreshTokenExpiresAtAfter(
            String refreshToken,
            LocalDateTime now
    );
}
