package com.duckstar.security.domain;

import com.duckstar.domain.Member;
import com.duckstar.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        indexes = {
                @Index(name = "idx_member_token_r",
                        columnList = "refresh_token")
        }
)
public class MemberToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Lob
    private String refreshToken;     // JWT refresh token

    // DB단 검증, 삭제 필요
    private LocalDateTime refreshTokenExpiresAt;

    protected MemberToken(
            Member member,
            String refreshToken,
            LocalDateTime refreshTokenExpiresAt
    ) {
        this.member = member;
        this.refreshToken = refreshToken;
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
    }

    public static MemberToken create(
            Member member,
            String refreshToken,
            LocalDateTime refreshTokenExpiresAt
    ) {
        return new MemberToken(
                member,
                refreshToken,
                refreshTokenExpiresAt
        );
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.refreshTokenExpiresAt);
    }
}