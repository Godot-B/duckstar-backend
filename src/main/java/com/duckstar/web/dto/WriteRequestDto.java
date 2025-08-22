package com.duckstar.web.dto;

import com.duckstar.validation.annotation.CommentConstraint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Getter;


public class WriteRequestDto {

    @CommentConstraint
    @Getter
    public static class CommentRequestDto {
        String attachedImageUrl;

        @Size(max = 1000)
        String body;
    }

    @Getter
    public static class ReplyRequestDto {
        Long listenerId;

        @Valid
        CommentRequestDto commentRequestDto;
    }
}
