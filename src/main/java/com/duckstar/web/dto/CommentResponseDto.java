package com.duckstar.web.dto;

import com.duckstar.domain.Member;
import com.duckstar.domain.enums.CommentStatus;
import com.duckstar.domain.mapping.Reply;
import com.duckstar.domain.mapping.comment.Comment;
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

        public static ReplyDto ofCreated(Reply reply, Member author, int voteCount) {
            Member listener = reply.getListener();

            return ReplyDto.builder()
                    .status(reply.getStatus())
                    .replyId(reply.getId())
                    .authorId(author.getId())
                    .canDeleteThis(true)

                    .isLiked(false)
                    .replyLikeId(null)
                    .likeCount(0)

                    .nickname(author.getNickname())
                    .profileImageUrl(author.getProfileImageUrl())
                    .voteCount(voteCount)

                    .createdAt(reply.getCreatedAt())
                    .listenerId(listener == null ? null : listener.getId())
                    .attachedImageUrl(reply.getAttachedImageUrl())
                    .body(reply.getBody())
                    .build();
        }
    }

    @Builder
    @Getter
    public static class DeleteResultDto {
        CommentStatus status;

        LocalDateTime createdAt;
        LocalDateTime deletedAt;
    }
}
