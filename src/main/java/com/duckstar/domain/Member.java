package com.duckstar.domain;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.AuthHandler;
import com.duckstar.domain.common.BaseEntity;
import com.duckstar.domain.enums.Gender;
import com.duckstar.security.domain.enums.MemberStatus;
import com.duckstar.security.domain.enums.OAuthProvider;
import com.duckstar.security.domain.enums.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_member_k", columnNames = "kakao_id"),
                @UniqueConstraint(name = "uk_member_n", columnNames = "naver_id"),
                @UniqueConstraint(name = "uk_member_g", columnNames = "google_id")
        }
)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String kakaoId;

    private String naverId;

    private String googleId;

    private String nickname;

    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 15, nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(length = 15, nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(length = 15, nullable = false)
    private MemberStatus status = MemberStatus.ACTIVE;

    protected Member(Long id) {
        this.id = id;
    }

    protected Member(
            String kakaoId,
            String naverId,
            String googleId,
            String nickname,
            String profileImageUrl,
            Gender gender,
            Role role
    ) {
        this.kakaoId = kakaoId;
        this.naverId = naverId;
        this.googleId = googleId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.gender = gender;
        this.role = role;
    }

    public static Member createKakao(
            OAuthProvider provider,
            String socialId,
            String nickname,
            String profileImageUrl,
            Gender gender,
            Role role
    ) {
        if (provider == OAuthProvider.KAKAO) {
            return new Member(
                    socialId,
                    null,
                    null,
                    nickname,
                    profileImageUrl,
                    gender,
                    role
            );
        } else {
            throw new AuthHandler(ErrorStatus.UNSUPPORTED_OAUTH_TYPE);
        }
    }

    public void withdraw() {
        this.status = MemberStatus.INACTIVE;
        if (kakaoId != null) {
            kakaoId = kakaoId + "_retired_" + UUID.randomUUID();
        }
        if (naverId != null) {
            naverId = naverId + "_retired_" + UUID.randomUUID();
        }
        if (googleId != null) {
            googleId = googleId + "_retired_" + UUID.randomUUID();
        }
        this.nickname = "탈퇴한 회원";
        this.profileImageUrl = null;
    }
}
