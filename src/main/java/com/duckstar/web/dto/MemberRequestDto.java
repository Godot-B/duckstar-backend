package com.duckstar.web.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

public class MemberRequestDto {

    @Getter
    @Setter
    public static class ProfileRequestDto {
        Boolean isSkip;

        String nickname;

        MultipartFile image;
    }
}
