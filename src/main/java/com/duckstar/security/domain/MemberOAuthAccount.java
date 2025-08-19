package com.duckstar.security.domain;

import com.duckstar.domain.Member;
import com.duckstar.security.domain.enums.OAuthProvider;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "member_oauth_account",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_member_oauth_pp",
                        columnNames = {"provider", "provider_user_id"})
        }
)
public class MemberOAuthAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(length = 15, nullable = false)
    private OAuthProvider provider;

    @Column(length = 80, nullable = false)
    private String providerUserId;

    @Lob
    private String accessToken;

    @Lob
    private String refreshToken;

    private LocalDateTime accessTokenExpiresAt;

    private LocalDateTime refreshTokenExpiresAt;

    protected MemberOAuthAccount(
            Member member,
            OAuthProvider provider,
            String providerUserId
    ) {
        this.member = member;
        this.provider = provider;
        this.providerUserId = providerUserId;
    }

    public static MemberOAuthAccount link(
            Member member,
            OAuthProvider provider,
            String providerId
    ) {
        return new MemberOAuthAccount(
                member,
                provider,
                providerId
        );
    }

    public void updateAccessToken(String accessToken, LocalDateTime accessTokenExpiresAt) {
        this.accessToken = accessToken;
        this.accessTokenExpiresAt = accessTokenExpiresAt;
    }

    public void updateRefreshToken(String refreshToken, LocalDateTime refreshTokenExpiresAt) {
        this.refreshToken = refreshToken;
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
    }
}
