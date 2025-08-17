package com.duckstar.member.infra.kakao.dto;

import com.duckstar.member.domain.Member;
import com.duckstar.member.domain.OauthId;
import com.duckstar.member.domain.OauthServerType;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.*;

@JsonNaming(SnakeCaseStrategy.class)
public record KakaoMemberReponse(
        Long id,
        boolean hasSignedUp,
        LocalDateTime connectedAt,
        KakaoAccount kakaoAccount
) {

    public Member toDomain() {
        return Member.create(
                new OauthId(String.valueOf(id), OauthServerType.KAKAO),
                kakaoAccount.profile.nickname,
                kakaoAccount.profile.profileImageUrl
        );
    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record KakaoAccount(
            boolean profileNeedsAgreement,
            boolean profileNicknameNeedsAgreement,
            boolean profileImageNeedsAgreement,
            Profile profile,
            boolean nameNeedsAgreement,
            String name,
            boolean emailNeedsAgreement,
            boolean isEmailValid,
            boolean isEmailVerified,
            String email,
            boolean ageRangeNeedsAgreement,
            String ageRange,
            boolean birthyearNeedsAgreement,
            String birthyear,
            boolean birthdayNeedsAgreement,
            String birthday,
            String birthdayType,
            boolean genderNeedsAgreement,
            String gender,
            boolean phoneNumberNeedsAgreement,
            String phoneNumber,
            boolean ciNeedsAgreement,
            String ci,
            LocalDateTime ciAuthenticatedAt
    ) {
    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record Profile(
            String nickname,
            String thumbnailImageUrl,
            String profileImageUrl,
            boolean isDefaultImage
    ) {
    }
}
