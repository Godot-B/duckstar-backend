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
        CommentStatus status;
        Long commentId;

        Long authorId;
        Boolean canDeleteThis;

        Boolean isLiked;
        Long commentLikeId;
        Integer likeCount;

        String nickname;
        String profileImageUrl;
        Integer voteCount;
        LocalDateTime createdAt;

        String attachedImageUrl;
        String body;

        Integer replyCount;
    }

    @Builder
    @Getter
    public static class ReplyDto {
        CommentStatus status;
        Long replyId;

        Long authorId;
        Boolean canDeleteThis;

        Boolean isLiked;
        Long replyLikeId;
        Integer likeCount;

        String nickname;
        String profileImageUrl;
        Integer voteCount;
        LocalDateTime createdAt;

        Long listenerId;
        String attachedImageUrl;
        String body;
    }

    @Builder
    @Getter
    public static class DeleteResultDto {
        CommentStatus status;

        LocalDateTime createdAt;
        LocalDateTime deletedAt;
    }
}
