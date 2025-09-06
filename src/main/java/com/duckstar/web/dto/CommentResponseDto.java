package com.duckstar.web.dto;

import com.duckstar.domain.Member;
import com.duckstar.domain.enums.CommentStatus;
import com.duckstar.domain.mapping.Reply;
import com.duckstar.domain.mapping.comment.Comment;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public class CommentResponseDto {

    @Builder
    @Getter
    public static class AnimeCommentSliceDto {

        // 첫 슬라이스에서만 댓,답글 개수 보내기
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Integer totalCount;

        List<CommentDto> commentDtos;

        PageInfo pageInfo;
    }

    @Builder
    @Getter
    public static class ReplySliceDto {
        List<ReplyDto> replyDtos;

        PageInfo pageInfo;
    }

    @Builder
    @Getter
    public static class CommentDto {
        CommentStatus status;
        Long commentId;

        Boolean canDeleteThis;

        Boolean isLiked;
        Long commentLikeId;
        Integer likeCount;

        Long authorId;
        String nickname;
        String profileImageUrl;
        Integer voteCount;

        Integer episodeNumber;

        LocalDateTime createdAt;
        String attachedImageUrl;
        String body;

        Integer replyCount;

        public static CommentDto ofCreated(
                Comment comment,
                Member author,
                int voteCount
        ) {
            return CommentDto.builder()
                    .status(comment.getStatus())
                    .commentId(comment.getId())
                    .canDeleteThis(true)

                    .isLiked(false)
                    .commentLikeId(null)
                    .likeCount(0)

                    .authorId(author.getId())
                    .nickname(author.getNickname())
                    .profileImageUrl(author.getProfileImageUrl())
                    .voteCount(voteCount)

                    .createdAt(comment.getCreatedAt())
                    .attachedImageUrl(comment.getAttachedImageUrl())
                    .body(comment.getBody())

                    .replyCount(0)
                    .build();
        }
    }

    @Builder
    @Getter
    public static class ReplyDto {
        CommentStatus status;
        Long replyId;

        Boolean canDeleteThis;

        Boolean isLiked;
        Long replyLikeId;
        Integer likeCount;

        Long authorId;
        String nickname;
        String profileImageUrl;
        Integer voteCount;

        LocalDateTime createdAt;
        Long listenerId;
        String attachedImageUrl;
        String body;

        public static ReplyDto ofCreated(
                Reply reply,
                Member author,
                int voteCount
        ) {
            Member listener = reply.getListener();

            return ReplyDto.builder()
                    .status(reply.getStatus())
                    .replyId(reply.getId())
                    .canDeleteThis(true)

                    .isLiked(false)
                    .replyLikeId(null)
                    .likeCount(0)

                    .authorId(author.getId())
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
