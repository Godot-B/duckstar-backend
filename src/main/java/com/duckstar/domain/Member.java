package com.duckstar.domain;

import com.duckstar.domain.common.BaseEntity;
import com.duckstar.domain.enums.Gender;
import com.duckstar.security.domain.enums.MemberStatus;
import com.duckstar.security.domain.enums.OAuthProvider;
import com.duckstar.security.domain.enums.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        indexes = {
                @Index(name = "idx_member_pp",
                        columnList = "provider, provider_id")
        }
)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nickname;

    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(15)", nullable = false)
    private Gender gender = Gender.UNKNOWN;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(15)", nullable = false)
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(15)", nullable = false)
    private MemberStatus status = MemberStatus.ACTIVE;

    private Boolean profileInitialized = false;


    /**
     * 현재는 Member <-> 소셜 계정
     *  - 1대1 , 소셜 연동 생각 X
     */

//    // 자체 회원가입
//    @Column(unique = true)
//    private String email;
//    private String password; // BCrypt, nullable if 소셜 로그인

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(15)")
    private OAuthProvider provider; // KAKAO, NAVER, GOOGLE, LOCAL 등

    private String providerId;  // 소셜 서비스에서 내려준 고유 ID

    /**
     * 현재는 구글과 네이버만 소셜 리프레시 토큰 저장
     *  - For 소셜 연동 해제
     */
    @Lob
    private String socialRefreshToken;

    // 혹시나 소셜 API 추가 이용에 대비
    private LocalDateTime socialRefreshTokenExpiresAt;

    /**
     * 최장 투표 기간
     * 현재 스트릭
     */

    private Integer bestStreak = 0;

    private Integer currentStreak = 0;

    @Builder
    protected Member(
//            String email,
//            String password,
            OAuthProvider provider,
            String providerId,
            String nickname,
            String profileImageUrl
    ) {
//        this.email = email;
//        this.password = password;
        this.provider = provider;
        this.providerId = providerId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }

//    public static Member createLocal(String email, String password, String nickname) {
//        return Member.builder()
//                .email(email)
//                .password(password)
//                .provider(null)
//                .providerId(null)
//                .nickname(nickname)
//                .profileImageUrl(null)
//                .build();
//    }

    public static Member createSocial(
            OAuthProvider provider,
            String providerId,
            String nickname,
            String profileImageUrl
    ) {
        return Member.builder()
//                .email(null)
//                .password(null)
                .provider(provider)
                .providerId(providerId)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .build();
    }

    public void restore(
            OAuthProvider provider,
            String providerId,
            String nickname,
            String profileImageUrl
    ) {
        this.status = MemberStatus.ACTIVE;

        this.provider = provider;
        this.providerId = providerId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.role = Role.USER;
    }

    public void setSocialRefreshToken(String socialRefreshToken, LocalDateTime socialRefreshTokenExpiresAt) {
        this.socialRefreshToken = socialRefreshToken;
        this.socialRefreshTokenExpiresAt = socialRefreshTokenExpiresAt;
    }

    public void updateProfile(String nickname, String profileImageUrl) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }

    public void updateInitializeInfo() {
        if (!this.profileInitialized)
            this.profileInitialized = true;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void updateStreak(boolean isConsecutive) {
        if (isConsecutive) {
            currentStreak += 1;
        } else {
            currentStreak = 1;
        }
        bestStreak = Math.max(bestStreak, currentStreak);
    }

    public void withdraw() {
        this.status = MemberStatus.INACTIVE;

        UUID uuid = UUID.randomUUID();
//        if (this.email != null) {
//            this.email = "retired_" + uuid + "@duckstar.kr";
//        }
//        this.password = null;
        this.nickname = "탈퇴한 회원_" + uuid;
        this.profileImageUrl = null;
        this.gender = Gender.UNKNOWN;
        this.role = Role.NONE;
        this.profileInitialized = false;
        this.socialRefreshToken = null;
        this.socialRefreshTokenExpiresAt = null;
    }
}
