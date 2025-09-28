package com.duckstar.web.dto;

import com.duckstar.domain.Member;
import com.duckstar.security.domain.enums.OAuthProvider;
import com.duckstar.security.domain.enums.Role;
import lombok.Builder;
import lombok.Getter;

public class MemberResponseDto {

    @Builder
    @Getter
    public static class UpdateReceiptDto {
        Boolean isChanged;

        MePreviewDto mePreviewDto;
    }

    @Builder
    @Getter
    public static class MePreviewDto {
        Long id;
        OAuthProvider provider;
        String nickname;
        String profileImageUrl;
        Role role;
        Boolean isProfileInitialized;

        public static MePreviewDto ofEmpty() {
            return MePreviewDto.builder().build();
        }

        public static MePreviewDto of(Member member) {
            return MePreviewDto.builder()
                    .id(member.getId())
                    .provider(member.getProvider())
                    .nickname(member.getNickname())
                    .profileImageUrl(member.getProfileImageUrl())
                    .role(member.getRole())
                    .isProfileInitialized(member.getProfileInitialized())
                    .build();
        }
    }
}
