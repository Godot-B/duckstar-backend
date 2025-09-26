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

//    // 자체 회원가입
//    @Column(unique = true)
//    private String email;
//    private String password; // BCrypt, nullable if 소셜 로그인

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private OAuthProvider provider; // KAKAO, NAVER, GOOGLE, LOCAL 등

    @Column(unique = true)
    private String providerId; // 소셜 서비스에서 내려준 고유 ID

    private String nickname;

    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 15, nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(length = 15, nullable = false)
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(length = 15, nullable = false)
    private MemberStatus status = MemberStatus.ACTIVE;

    protected Member(Long id) {
        this.id = id;
    }

    @Builder
    protected Member(
//            String email,
//            String password,
            OAuthProvider provider,
            String providerId,
            String nickname,
            String profileImageUrl,
            Gender gender
    ) {
//        this.email = email;
//        this.password = password;
        this.provider = provider;
        this.providerId = providerId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.gender = gender;
    }

//    public static Member createLocal(String email, String password, String nickname) {
//        return Member.builder()
//                .email(email)
//                .password(password)
//                .provider(null)
//                .providerId(null)
//                .nickname(nickname)
//                .profileImageUrl(null)
//                .gender(null)
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
                .gender(null)
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
    }

    public void updateProfile(String nickname, String profileImageUrl) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
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
        this.gender = null;
    }
}
