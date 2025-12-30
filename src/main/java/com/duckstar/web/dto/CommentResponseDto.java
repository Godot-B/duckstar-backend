package com.duckstar.web.dto;

import com.duckstar.domain.Member;
import com.duckstar.domain.enums.CommentStatus;
import com.duckstar.domain.mapping.*;
import com.duckstar.domain.mapping.comment.AnimeComment;
import com.duckstar.domain.mapping.weeklyVote.EpisodeStar;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
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
        // 첫 슬라이스에서만 답글 개수 보내기
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Integer totalCount;

        List<ReplyDto> replyDtos;

        PageInfo pageInfo;
    }

    @Builder
    @Getter
    @AllArgsConstructor
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

        // 늦참 기능에 의해 추가 (2025/11/25)
        Integer starScore;
        Boolean isLateParticipating;

        // 추가 (2025년 12월 24일)
        Long surveyCandidateId;

        public static CommentDto ofCreated(
                AnimeComment comment,
                Member author,
                int voteCount
        ) {
            EpisodeStar episodeStar = comment.getEpisodeStar();

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

                    .episodeNumber(
                            Boolean.TRUE.equals(comment.getIsUserTaggedEp()) ?
                                    comment.getEpisode().getEpisodeNumber() :
                                    null
                    )

                    .createdAt(comment.getCreatedAt())
                    .attachedImageUrl(comment.getAttachedImageUrl())
                    .body(comment.getBody())

                    .replyCount(0)

                    .starScore(
                            episodeStar != null ?
                                    episodeStar.getStarScore() :
                                    null
                    )
                    .isLateParticipating(
                            episodeStar != null ?
                                    episodeStar.getIsLateParticipating() :
                                    null
                    )
                    .build();
        }

        public static CommentDto ofDeleted(
                CommentStatus status,
                Long commentId,
                LocalDateTime createdAt,
                Integer episodeNumber,
                Integer replyCount
        ) {
            return CommentDto.builder()
                    .status(status)
                    .commentId(commentId)

                    .canDeleteThis(null)
                    .isLiked(null)
                    .commentLikeId(null)
                    .likeCount(null)
                    .authorId(null)
                    .nickname(null)
                    .profileImageUrl(null)
                    .voteCount(null)

                    .episodeNumber(episodeNumber)
                    .createdAt(createdAt)

                    .attachedImageUrl(null)
                    .body(null)

                    .replyCount(replyCount)
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
        String listenerNickname;
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
                    .listenerNickname(listener == null ? null : listener.getNickname())
                    .attachedImageUrl(reply.getAttachedImageUrl())
                    .body(reply.getBody())
                    .build();
        }
    }

    @Builder
    @Getter
    public static class LikeResultDto {
        Long likeId;
        Integer likeCount;
        LocalDateTime likedAt;

        public static LikeResultDto ofComment(CommentLike commentLike) {
            if (commentLike == null) {
                return LikeResultDto.builder().build();
            }

            return LikeResultDto.builder()
                    .likeId(commentLike.getId())
                    .likeCount(commentLike.getComment().getLikeCount())
                    .likedAt(LocalDateTime.now())
                    .build();
        }

        public static LikeResultDto ofReply(ReplyLike replyLike) {
            if (replyLike == null) {
                return LikeResultDto.builder().build();
            }

            return LikeResultDto.builder()
                    .likeId(replyLike.getId())
                    .likeCount(replyLike.getReply().getLikeCount())
                    .likedAt(LocalDateTime.now())
                    .build();
        }
    }

    @Builder
    @Getter
    public static class DiscardLikeResultDto {
        Integer likeCount;
        LocalDateTime discardedAt;
    }

    @Builder
    @Getter
    public static class DeleteResultDto {
        CommentStatus status;

        LocalDateTime createdAt;
        LocalDateTime deletedAt;
    }
}
