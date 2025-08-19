package com.duckstar.web.dto;

import com.duckstar.domain.enums.CommentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public class CommentResponseDto {

    @Builder
    @Getter
    public static class AnimeCommentSliceDto {
        List<CommentDto> commentDtos;

        PageInfo pageInfo;
    }

    @Builder
    @Getter
    public static class CommentDto {
        Boolean isLiked;

        CommentStatus status;

        String nickname;

        String profileImageUrl;

        Integer voteCount;

        LocalDateTime createdAt;

        String attachedImageUrl;

        String body;

        Integer replyCount;

        List<ReplyDto> replyDtos;  // 특정 조건에 해당하면 미리 로드
    }

    @Builder
    @Getter
    public static class ReplyDto {
        Boolean isLiked;

        CommentStatus status;

        String nickname;

        String profileImageUrl;

        Integer voteCount;

        LocalDateTime createdAt;

        String attachedImageUrl;

        String body;
    }
}
