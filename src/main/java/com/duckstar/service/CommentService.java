package com.duckstar.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.*;
import com.duckstar.domain.Anime;
import com.duckstar.domain.Member;
import com.duckstar.domain.enums.CommentSortType;
import com.duckstar.domain.enums.CommentStatus;
import com.duckstar.domain.mapping.CommentLike;
import com.duckstar.domain.mapping.Episode;
import com.duckstar.domain.mapping.Reply;
import com.duckstar.domain.mapping.comment.AnimeComment;
import com.duckstar.repository.AnimeComment.AnimeCommentRepository;
import com.duckstar.repository.AnimeVote.AnimeVoteRepository;
import com.duckstar.repository.CommentLikeRepository;
import com.duckstar.repository.Episode.EpisodeRepository;
import com.duckstar.repository.Reply.ReplyRepository;
import com.duckstar.s3.S3Uploader;
import com.duckstar.security.MemberPrincipal;
import com.duckstar.web.dto.CommentResponseDto.CommentDto;
import com.duckstar.web.dto.CommentResponseDto.DeleteResultDto;
import com.duckstar.web.dto.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

import static com.duckstar.web.dto.CommentResponseDto.*;
import static com.duckstar.web.dto.BoardRequestDto.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final AnimeCommentRepository animeCommentRepository;
    private final MemberService memberService;
    private final AnimeVoteRepository animeVoteRepository;
    private final EpisodeRepository episodeRepository;
    private final AnimeService animeService;
    private final S3Uploader s3Uploader;
    private final CommentLikeRepository commentLikeRepository;

    public AnimeComment findByIdOrThrow(Long commentId) {
        return animeCommentRepository.findById(commentId).orElseThrow(() ->
                new CommentHandler(ErrorStatus.COMMENT_NOT_FOUND));
    }

    private CommentLike findLikeByIdOrThrow(Long commentLikeId) {
        return commentLikeRepository.findById(commentLikeId)
                .orElseThrow(() -> new LikeHandler(ErrorStatus.LIKE_NOT_FOUND));
    }

    @Transactional
    public CommentDto leaveAnimeComment(
            Long animeId,
            CommentRequestDto request,
            MemberPrincipal principal
    ) {
        if (principal == null) {
            throw new AuthHandler(ErrorStatus.POST_UNAUTHORIZED);
        }

        Anime anime = animeService.findByIdOrThrow(animeId);

        Long memberId = principal.getId();
        Member author = memberService.findByIdOrThrow(memberId);

        int voteCount = animeVoteRepository
                .countAllByAnimeCandidate_Anime_IdAndWeekVoteSubmission_Member_Id(
                        animeId,
                        memberId
                );

        boolean isUserTaggedEp = false;
        Long episodeId = request.getEpisodeId();

        Episode episode;
        if (episodeId != null) {
            isUserTaggedEp = true;
            episode = episodeRepository.findById(episodeId).orElseThrow(() ->
                    new EpisodeHandler(ErrorStatus.EPISODE_NOT_FOUND));

            if (episode.getScheduledAt().isAfter(LocalDateTime.now())) {
                throw new CommentHandler(ErrorStatus.CANNOT_POST_BEFORE_EPISODE_START);
            }
        } else {
            LocalDateTime now = LocalDateTime.now();
            episode = findCurrentEpisode(anime, now)
                    .orElse(null);
        }

        String imageUrl = null;
        MultipartFile image = request.getAttachedImage();
        if (image != null && !image.isEmpty()) {
            imageUrl = s3Uploader.uploadWithUUID(image, "comments");
        }

        AnimeComment animeComment = AnimeComment.create(
                anime,
                episode,
                author,
                isUserTaggedEp,
                voteCount,
                imageUrl,
                request.getBody()
        );

        AnimeComment saved = animeCommentRepository.save(animeComment);

        return CommentDto.ofCreated(saved, author, voteCount);
    }

    private Optional<Episode> findCurrentEpisode(Anime anime, LocalDateTime now) {
        return episodeRepository
                .findEpisodeByAnimeAndScheduledAtLessThanEqualAndNextEpScheduledAtGreaterThan(anime, now, now);
    }

    public AnimeCommentSliceDto getAnimeCommentSliceDto(
            Long animeId,
            List<Long> episodeIds,
            CommentSortType sortBy,
            Pageable pageable,
            MemberPrincipal principal
    ) {
        if (episodeIds == null) {
            episodeIds = List.of();
        }

        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();

        Integer totalCount = null;
        if (page == 0) {
            totalCount = animeCommentRepository.countTotalElements(animeId, episodeIds);
        }

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
                .totalCount(totalCount)
                .commentDtos(rows)
                .pageInfo(pageInfo)
                .build();
    }

    @Transactional
    public LikeResultDto giveLike(
            Long commentId,
            Long commentLikeId,
            MemberPrincipal principal
    ) {
        AnimeComment comment = findByIdOrThrow(commentId);

        if (principal == null) {
            throw new AuthHandler(ErrorStatus.PRINCIPAL_NOT_FOUND);
        }
        Member member = memberService.findByIdOrThrow(principal.getId());

        CommentLike commentLike = null;
        if (commentLikeId != null) {
            commentLike = findLikeByIdOrThrow(commentLikeId);

            if (!Objects.equals(commentLike.getComment(), comment) ||
                    !Objects.equals(commentLike.getMember(), member)) {
                throw new LikeHandler(ErrorStatus.LIKE_UNAUTHORIZED);
            }

            commentLike.restoreLike();
        } else {
            Optional<CommentLike> likeOpt = commentLikeRepository.findByCommentAndMember(comment, member);
            // 여기서 존재하는 경우는 그냥 무시-> 빈 dto 반환

            if (likeOpt.isEmpty()) {
                commentLike = CommentLike.create(comment, member);
                commentLikeRepository.save(commentLike);
            }
        }

        return LikeResultDto.ofComment(commentLike);
    }

    @Transactional
    public DiscardLikeResultDto discardLike(
            Long commentId,
            Long commentLikeId,
            MemberPrincipal principal
    ) {
        AnimeComment comment = findByIdOrThrow(commentId);

        if (principal == null) {
            throw new AuthHandler(ErrorStatus.PRINCIPAL_NOT_FOUND);
        }
        Member member = memberService.findByIdOrThrow(principal.getId());

        if (commentLikeId == null) {
            throw new AuthHandler(ErrorStatus.LIKE_NOT_FOUND);
        }
        CommentLike commentLike = findLikeByIdOrThrow(commentLikeId);

        if (!Objects.equals(commentLike.getComment(), comment) ||
                !Objects.equals(commentLike.getMember(), member)) {
            throw new LikeHandler(ErrorStatus.DISLIKE_UNAUTHORIZED);
        }

        commentLike.discardLike();

        return DiscardLikeResultDto.builder()
                .likeCount(commentLike.getComment().getLikeCount())
                .discardedAt(commentLike.getUpdatedAt())
                .build();
    }

    @Transactional
    public DeleteResultDto deleteAnimeComment(
            Long commentId,
            MemberPrincipal principal
    ) {
        AnimeComment comment = findByIdOrThrow(commentId);

        if (principal == null) {
            throw new AuthHandler(ErrorStatus.DELETE_UNAUTHORIZED);
        }

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
