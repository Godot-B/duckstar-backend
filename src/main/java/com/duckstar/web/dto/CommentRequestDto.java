package com.duckstar.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CommentRequestDto {
    String attachedImageUrl;

    @NotBlank @Max(1000)
    String body;
}
