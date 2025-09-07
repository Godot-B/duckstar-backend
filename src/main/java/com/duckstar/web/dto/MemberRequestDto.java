package com.duckstar.web.dto;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

public class MemberRequestDto {

    @Getter
    public static class ProfileRequestDto {
        String nickname;

        MultipartFile image;
    }
}
