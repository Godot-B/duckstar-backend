package com.duckstar.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.CommentHandler;
import com.duckstar.domain.Anime;
import com.duckstar.domain.Member;
import com.duckstar.domain.enums.CommentSortType;
import com.duckstar.domain.enums.CommentStatus;
import com.duckstar.domain.mapping.comment.AnimeComment;
import com.duckstar.repository.AnimeComment.AnimeCommentRepository;
import com.duckstar.repository.AnimeRepository;
import com.duckstar.repository.WeekVoteSubmissionRepository;
import com.duckstar.security.MemberPrincipal;
import com.duckstar.security.repository.MemberRepository;
import com.duckstar.web.dto.CommentRequestDto;
import com.duckstar.web.dto.CommentResponseDto.CommentDto;
import com.duckstar.web.dto.CommentResponseDto.DeleteResultDto;
import com.duckstar.web.dto.CommentResponseDto.ReplyDto;
import com.duckstar.web.dto.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.duckstar.web.dto.CommentResponseDto.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final AnimeRepository animeRepository;
    private final MemberRepository memberRepository;
    private final WeekVoteSubmissionRepository weekVoteSubmissionRepository;
    private final AnimeCommentRepository animeCommentRepository;

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
                .build();
    }

    public AnimeCommentSliceDto getAnimeCommentSliceDto(
            Long animeId,
            CommentSortType sortBy,
            Pageable pageable,
            MemberPrincipal principal
    ) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();

        Pageable overFetch = PageRequest.of(
                page,
                size + 1,
                pageable.getSort()
        );

        List<CommentDto> rows = animeCommentRepository.getCommentDtos(
                animeId,
                sortBy,
                overFetch,
                principal
        );

        boolean commentsHasNext = rows.size() > size;

        if (commentsHasNext) rows = rows.subList(0, size);

        PageInfo pageInfo = PageInfo.builder()
                .hasNext(commentsHasNext)
                .page(page)
                .size(size)
                .build();

        return AnimeCommentSliceDto.builder()
                .commentDtos(rows)
                .pageInfo(pageInfo)
                .build();
    }

    @Transactional
    public DeleteResultDto deleteAnimeComment(
            Long commentId,
            MemberPrincipal principal
    ) {
        AnimeComment comment = animeCommentRepository.findById(commentId).orElseThrow(() ->
                new CommentHandler(ErrorStatus.COMMENT_NOT_FOUND));

        boolean isAuthor = Objects.equals(comment.getAuthor().getId(), principal.getId());
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
