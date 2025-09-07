package com.duckstar.web.dto;

import com.duckstar.validation.annotation.CommentConstraint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;


public class WriteRequestDto {

    @CommentConstraint
    @Getter
    @Setter
    public static class CommentRequestDto {
        Long episodeId;

        MultipartFile attachedImage;

        @Size(max = 1000)
        String body;
    }

    @Getter
    @Setter
    public static class ReplyRequestDto {
        Long listenerId;

        @Valid
        CommentRequestDto commentRequestDto;
    }
}
