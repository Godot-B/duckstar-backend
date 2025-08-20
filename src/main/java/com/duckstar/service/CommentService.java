package com.duckstar.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.AnimeHandler;
import com.duckstar.apiPayload.exception.handler.CommentHandler;
import com.duckstar.apiPayload.exception.handler.ReplyHandler;
import com.duckstar.domain.Anime;
import com.duckstar.domain.Member;
import com.duckstar.domain.enums.CommentSortType;
import com.duckstar.domain.enums.CommentStatus;
import com.duckstar.domain.mapping.Reply;
import com.duckstar.domain.mapping.comment.AnimeComment;
import com.duckstar.repository.AnimeComment.AnimeCommentRepository;
import com.duckstar.repository.AnimeRepository;
import com.duckstar.repository.AnimeVote.AnimeVoteRepository;
import com.duckstar.repository.Reply.ReplyRepository;
import com.duckstar.repository.WeekVoteSubmissionRepository;
import com.duckstar.security.MemberPrincipal;
import com.duckstar.web.dto.CommentResponseDto.CommentDto;
import com.duckstar.web.dto.CommentResponseDto.DeleteResultDto;
import com.duckstar.web.dto.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.duckstar.web.dto.CommentResponseDto.*;
import static com.duckstar.web.dto.WriteRequestDto.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final AnimeRepository animeRepository;
    private final AnimeCommentRepository animeCommentRepository;
    private final ReplyRepository replyRepository;
    private final MemberService memberService;
    private final AnimeVoteRepository animeVoteRepository;

    @Transactional
    public CommentDto leaveAnimeComment(
            Long animeId,
            CommentRequestDto request,
            Long principalId
    ) {
        Anime anime = animeRepository.findById(animeId).orElseThrow(() ->
                new AnimeHandler(ErrorStatus.ANIME_NOT_FOUND));

        Member author = memberService.findByIdOrThrow(principalId);

        int voteCount = animeVoteRepository
                .countAllByAnimeCandidate_Anime_IdAndWeekVoteSubmission_Member_Id(
                        animeId,
                        principalId
                );


        AnimeComment animeComment = AnimeComment.create(
                anime,
                author,
                voteCount,
                request.getAttachedImageUrl(),   // S3
                request.getBody()
        );

        AnimeComment saved = animeCommentRepository.save(animeComment);

        return CommentDto.ofCreated(saved, author, voteCount);
    }

    public AnimeCommentSliceDto getAnimeCommentSliceDto(
            Long animeId,
            List<Long> episodeIds,
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
                episodeIds,
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
        AnimeComment comment = findByIdOrThrow(commentId);

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

    @Transactional
    public ReplyDto leaveReply(
            Long commentId,
            ReplyRequestDto request,
            Long principalId
    ) {
        AnimeComment comment = findByIdOrThrow(commentId);

        Member author = memberService.findByIdOrThrow(principalId);

        int voteCount = animeVoteRepository
                .countAllByAnimeCandidate_Anime_IdAndWeekVoteSubmission_Member_Id(
                        comment.getAnime().getId(),
                        principalId
                );

        Long listenerId = request.getListenerId();
        Member listener = listenerId != null ?
                memberService.findByIdOrThrow(listenerId) :
                null;

        CommentRequestDto content = request.getCommentRequestDto();

        Reply reply = Reply.create(
                comment,
                author,
                Optional.ofNullable(listener),
                voteCount,
                content.getAttachedImageUrl(),  // S3
                content.getBody()
        );

        Reply saved = replyRepository.save(reply);

        return ReplyDto.ofCreated(
                saved,
                author,
                voteCount
        );
    }

    public AnimeComment findByIdOrThrow(Long commentId) {
        return animeCommentRepository.findById(commentId).orElseThrow(() ->
                new CommentHandler(ErrorStatus.COMMENT_NOT_FOUND));
    }

    public ReplySliceDto getReplySliceDto(
            Long commentId,
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

        List<ReplyDto> rows = replyRepository.getReplyDtos(
                commentId,
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

        return ReplySliceDto.builder()
                .replyDtos(rows)
                .pageInfo(pageInfo)
                .build();
    }

    @Transactional
    public DeleteResultDto deleteReply(Long replyId, MemberPrincipal principal) {
        Reply reply = replyRepository.findById(replyId).orElseThrow(() ->
                new ReplyHandler(ErrorStatus.REPLY_NOT_FOUND));

        boolean isAuthor = Objects.equals(reply.getAuthor().getId(), principal.getId());
        boolean isAdmin = principal.isAdmin();

        if (isAuthor) {
            reply.setStatus(CommentStatus.DELETED);

        } else if (isAdmin) {
            reply.setStatus(CommentStatus.ADMIN_DELETED);

        } else {
            throw new CommentHandler(ErrorStatus.DELETE_UNAUTHORIZED);
        }

        return DeleteResultDto.builder()
                .status(reply.getStatus())
                .createdAt(reply.getCreatedAt())
                .deletedAt(reply.getUpdatedAt())
                .build();
    }
}
