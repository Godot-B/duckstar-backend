package com.duckstar.security.domain;

import com.duckstar.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Lob
    private String refreshToken;     // JWT refresh token

    private LocalDateTime refreshTokenExpiresAt;

    private Boolean isValid = true;         // 로그아웃 시 false

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

    public void invalidate() {
        isValid = false;
    }

    public void validate() {
        isValid = true;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.refreshTokenExpiresAt);
    }

    public boolean isValid() {
        return isValid;
    }
}