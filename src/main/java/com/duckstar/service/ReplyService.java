package com.duckstar.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.*;
import com.duckstar.domain.Member;
import com.duckstar.domain.enums.CommentStatus;
import com.duckstar.domain.mapping.CommentLike;
import com.duckstar.domain.mapping.Reply;
import com.duckstar.domain.mapping.ReplyLike;
import com.duckstar.domain.mapping.comment.AnimeComment;
import com.duckstar.repository.AnimeComment.AnimeCommentRepository;
import com.duckstar.repository.AnimeVote.AnimeVoteRepository;
import com.duckstar.repository.Reply.ReplyRepository;
import com.duckstar.repository.ReplyLikeRepository;
import com.duckstar.s3.S3Uploader;
import com.duckstar.security.MemberPrincipal;
import com.duckstar.security.repository.MemberRepository;
import com.duckstar.web.dto.BoardRequestDto;
import com.duckstar.web.dto.CommentResponseDto;
import com.duckstar.web.dto.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.duckstar.web.dto.BoardRequestDto.*;
import static com.duckstar.web.dto.CommentResponseDto.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final AnimeVoteRepository animeVoteRepository;
    private final ReplyLikeRepository replyLikeRepository;
    private final MemberRepository memberRepository;
    private final AnimeCommentRepository animeCommentRepository;

    private final S3Uploader s3Uploader;

    private ReplyLike findLikeByIdOrThrow(Long replyLikeId) {
        return replyLikeRepository.findById(replyLikeId)
                .orElseThrow(() -> new LikeHandler(ErrorStatus.LIKE_NOT_FOUND));
    }

    @Transactional
    public ReplyDto leaveReply(
            Long commentId,
            ReplyRequestDto request,
            MemberPrincipal principal
    ) {
        if (principal == null) {
            throw new AuthHandler(ErrorStatus.POST_UNAUTHORIZED);
        }

        AnimeComment comment = animeCommentRepository.findById(commentId).orElseThrow(() ->
                new CommentHandler(ErrorStatus.COMMENT_NOT_FOUND));

        Long memberId = principal.getId();
        Member author = memberRepository.findById(memberId).orElseThrow(() ->
                new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        int voteCount = animeVoteRepository
                .countAllByAnimeCandidate_Anime_IdAndWeekVoteSubmission_Member_Id(
                        comment.getAnime().getId(),
                        memberId
                );

        Long listenerId = request.getListenerId();
        Member listener = listenerId != null ?
                memberRepository.findById(listenerId).orElseThrow(() ->
                        new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND)) :
                null;

        CommentRequestDto content = request.getCommentRequestDto();

        String imageUrl = null;
        MultipartFile image = content.getAttachedImage();
        if (image != null && !image.isEmpty()) {
            imageUrl = s3Uploader.uploadWithUUID(image, "comments");
        }

        Reply reply = Reply.create(
                comment,
                author,
                listener,
                voteCount,
                imageUrl,  // S3
                content.getBody()
        );

        Reply saved = replyRepository.save(reply);

        return ReplyDto.ofCreated(
                saved,
                author,
                voteCount
        );
    }

    public ReplySliceDto getReplySliceDto(
            Long commentId,
            Pageable pageable,
            MemberPrincipal principal
    ) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();

        Integer totalCount = null;
        if (page == 0) {
            totalCount = replyRepository.countAllByParent_id(commentId);
        }

        Pageable overFetch = PageRequest.of(
                page,
                size + 1
        );

        List<ReplyDto> rows = replyRepository.getReplyDtos(
                commentId,
                overFetch,
                principal
        );

        boolean repliesHasNext = rows.size() > size;

        if (repliesHasNext) rows = rows.subList(0, size);

        PageInfo pageInfo = PageInfo.builder()
                .hasNext(repliesHasNext)
                .page(page)
                .size(size)
                .build();

        return ReplySliceDto.builder()
                .totalCount(totalCount)
                .replyDtos(rows)
                .pageInfo(pageInfo)
                .build();
    }

    @Transactional
    public LikeResultDto giveLike(
            Long replyId,
            Long replyLikeId,
            MemberPrincipal principal
    ) {
        if (principal == null) {
            throw new AuthHandler(ErrorStatus.PRINCIPAL_NOT_FOUND);
        }

        Reply reply = replyRepository.findById(replyId).orElseThrow(() ->
                new ReplyHandler(ErrorStatus.REPLY_NOT_FOUND));
        Member member = memberRepository.findById(principal.getId()).orElseThrow(() ->
                new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        ReplyLike replyLike = null;
        if (replyLikeId != null) {
            replyLike = findLikeByIdOrThrow(replyLikeId);

            if (!Objects.equals(replyLike.getReply(), reply) ||
                    !Objects.equals(replyLike.getMember(), member)) {
                throw new LikeHandler(ErrorStatus.LIKE_UNAUTHORIZED);
            }

            replyLike.restoreLike();
        } else {
            Optional<ReplyLike> likeOpt = replyLikeRepository.findByReplyAndMember(reply, member);
            // 여기서 존재하는 경우는 그냥 무시-> 빈 dto 반환

            if (likeOpt.isEmpty()) {
                replyLike = ReplyLike.create(reply, member);
                replyLike = replyLikeRepository.save(replyLike);
            }
        }

        return LikeResultDto.ofReply(replyLike);
    }

    @Transactional
    public DiscardLikeResultDto discardLike(
            Long replyId,
            Long replyLikeId,
            MemberPrincipal principal
    ) {
        if (principal == null) {
            throw new AuthHandler(ErrorStatus.PRINCIPAL_NOT_FOUND);
        }

        Reply reply = replyRepository.findById(replyId).orElseThrow(() ->
                new ReplyHandler(ErrorStatus.REPLY_NOT_FOUND));
        Member member = memberRepository.findById(principal.getId()).orElseThrow(() ->
                new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        if (replyLikeId == null) {
            throw new AuthHandler(ErrorStatus.LIKE_NOT_FOUND);
        }
        ReplyLike replyLike = findLikeByIdOrThrow(replyLikeId);

        if (!Objects.equals(replyLike.getReply(), reply) ||
                !Objects.equals(replyLike.getMember(), member)) {
            throw new LikeHandler(ErrorStatus.DISLIKE_UNAUTHORIZED);
        }

        replyLike.discardLike();

        return DiscardLikeResultDto.builder()
                .likeCount(replyLike.getReply().getLikeCount())
                .discardedAt(replyLike.getUpdatedAt())
                .build();
    }

    @Transactional
    public DeleteResultDto deleteReply(Long replyId, MemberPrincipal principal) {
        Reply reply = replyRepository.findById(replyId).orElseThrow(() ->
                new ReplyHandler(ErrorStatus.REPLY_NOT_FOUND));

        if (principal == null) {
            throw new AuthHandler(ErrorStatus.DELETE_UNAUTHORIZED);
        }

        boolean isAuthor = Objects.equals(reply.getAuthor().getId(), principal.getId());
        boolean isAdmin = principal.isAdmin();

        if (isAuthor) {
            reply.setStatus(CommentStatus.DELETED);
            reply.getParent().removeReply();

        } else if (isAdmin) {
            reply.setStatus(CommentStatus.ADMIN_DELETED);
            reply.getParent().removeReply();

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
