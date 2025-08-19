package com.duckstar.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.CommentHandler;
import com.duckstar.domain.Anime;
import com.duckstar.domain.Member;
import com.duckstar.domain.enums.CommentStatus;
import com.duckstar.domain.mapping.comment.AnimeComment;
import com.duckstar.repository.AnimeCommentRepository;
import com.duckstar.repository.AnimeRepository;
import com.duckstar.repository.WeekVoteSubmissionRepository;
import com.duckstar.security.MemberPrincipal;
import com.duckstar.security.domain.enums.Role;
import com.duckstar.security.repository.MemberRepository;
import com.duckstar.web.dto.CommentRequestDto;
import com.duckstar.web.dto.CommentResponseDto.CommentDto;
import com.duckstar.web.dto.CommentResponseDto.DeleteResultDto;
import com.duckstar.web.dto.CommentResponseDto.ReplyDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final AnimeRepository animeRepository;
    private final MemberRepository memberRepository;
    private final WeekVoteSubmissionRepository weekVoteSubmissionRepository;
    private final AnimeCommentRepository animeCommentRepository;

    private static final List<ReplyDto> EMPTY_REPLY_LIST = List.of();

    @Transactional
    public CommentDto leaveAnimeComment(
            Long animeId,
            CommentRequestDto request,
            Long memberId
    ) {
        Anime anime = animeRepository.findById(animeId).orElseThrow(() ->
                new CommentHandler(ErrorStatus.ANIME_NOT_FOUND));

        Member member = memberRepository.findById(memberId).orElseThrow(() ->
                new CommentHandler(ErrorStatus.MEMBER_NOT_FOUND));

        int voteCount = weekVoteSubmissionRepository.countByMemberId(memberId);

        AnimeComment animeComment = AnimeComment.create(
                anime,
                member,
                voteCount,
                request.getAttachedImageUrl(),   // S3
                request.getBody()
        );

        AnimeComment saved = animeCommentRepository.save(animeComment);

        return CommentDto.builder()
                .status(CommentStatus.NORMAL)
                .commentId(saved.getId())

                .authorId(memberId)
                .canDeleteThis(true)

                .commentLikeId(null)
                .isLiked(false)

                .nickname(member.getNickname())
                .profileImageUrl(member.getProfileImageUrl())
                .voteCount(voteCount)
                .createdAt(saved.getCreatedAt())

                .attachedImageUrl(saved.getAttachedImageUrl())
                .body(saved.getBody())

                .replyCount(0)
                .replyDtos(EMPTY_REPLY_LIST)
                .build();
    }

    @Transactional
    public DeleteResultDto deleteAnimeComment(
            Long commentId,
            MemberPrincipal principal
    ) {
        AnimeComment comment = animeCommentRepository.findById(commentId).orElseThrow(() ->
                new CommentHandler(ErrorStatus.COMMENT_NOT_FOUND));

        boolean isAuthor = comment.getAuthor().getId().equals(principal.getId());
        boolean isAdmin = principal.isAdmin();

        if (isAuthor) {
            comment.setStatus(CommentStatus.DELETED);

        } else if (isAdmin) {
            comment.setStatus(CommentStatus.ADMIN_DELETED);

        } else {
            throw new CommentHandler(ErrorStatus.DELETE_UNAUTHORIZED);
        }

        return DeleteResultDto.builder()
                .status(comment.getStatus())
                .createdAt(comment.getCreatedAt())
                .deletedAt(comment.getUpdatedAt())
                .build();
    }
}
