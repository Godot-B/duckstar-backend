package com.duckstar.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.*;
import com.duckstar.domain.Anime;
import com.duckstar.domain.Member;
import com.duckstar.domain.enums.CommentSortType;
import com.duckstar.domain.enums.CommentStatus;
import com.duckstar.domain.mapping.CommentLike;
import com.duckstar.domain.mapping.weeklyVote.Episode;
import com.duckstar.domain.mapping.weeklyVote.EpisodeStar;
import com.duckstar.domain.mapping.comment.AnimeComment;
import com.duckstar.repository.AnimeComment.AnimeCommentRepository;
import com.duckstar.repository.AnimeRepository;
import com.duckstar.repository.CommentLikeRepository;
import com.duckstar.repository.Episode.EpisodeRepository;
import com.duckstar.repository.EpisodeStar.EpisodeStarRepository;
import com.duckstar.s3.S3Uploader;
import com.duckstar.security.MemberPrincipal;
import com.duckstar.security.repository.MemberRepository;
import com.duckstar.service.AnimeService.AnimeQueryService;
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
    private final EpisodeRepository episodeRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final MemberRepository memberRepository;
    private final AnimeRepository animeRepository;

    private final AnimeQueryService animeQueryService;
    private final S3Uploader s3Uploader;
    private final EpisodeStarRepository episodeStarRepository;

    @Transactional
    public CommentDto leaveAnimeComment(
            Long animeId,
            CommentRequestDto request,
            MemberPrincipal principal
    ) {
        if (principal == null) {
            throw new AuthHandler(ErrorStatus.POST_UNAUTHORIZED);
        }

        Anime anime = animeRepository.findById(animeId).orElseThrow(() ->
                new AnimeHandler(ErrorStatus.ANIME_NOT_FOUND));

        Long memberId = principal.getId();
        Member author = memberRepository.findById(memberId).orElseThrow(() ->
                new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        int voteCount = episodeStarRepository
                .countAllByEpisode_Anime_IdAndWeekVoteSubmission_Member_Id(
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
            episode = animeQueryService.findCurrentEpisode(anime, now)
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
        if (principal == null) {
            throw new AuthHandler(ErrorStatus.PRINCIPAL_NOT_FOUND);
        }

        AnimeComment comment = animeCommentRepository.findById(commentId).orElseThrow(() ->
                new CommentHandler(ErrorStatus.COMMENT_NOT_FOUND));
        Member member = memberRepository.findById(principal.getId()).orElseThrow(() ->
                new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        CommentLike commentLike = null;
        if (commentLikeId != null) {
            commentLike = commentLikeRepository.findById(commentLikeId)
                    .orElseThrow(() -> new LikeHandler(ErrorStatus.LIKE_NOT_FOUND));

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
                commentLike = commentLikeRepository.save(commentLike);
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
        if (principal == null) {
            throw new AuthHandler(ErrorStatus.PRINCIPAL_NOT_FOUND);
        }

        AnimeComment comment = animeCommentRepository.findById(commentId).orElseThrow(() ->
                new CommentHandler(ErrorStatus.COMMENT_NOT_FOUND));
        Member member = memberRepository.findById(principal.getId()).orElseThrow(() ->
                new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        if (commentLikeId == null) {
            throw new AuthHandler(ErrorStatus.LIKE_NOT_FOUND);
        }
        CommentLike commentLike = commentLikeRepository.findById(commentLikeId)
                .orElseThrow(() -> new LikeHandler(ErrorStatus.LIKE_NOT_FOUND));

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
        if (principal == null) {
            throw new AuthHandler(ErrorStatus.DELETE_UNAUTHORIZED);
        }

        AnimeComment comment = animeCommentRepository.findById(commentId).orElseThrow(() ->
                new CommentHandler(ErrorStatus.COMMENT_NOT_FOUND));

        boolean isAuthor = Objects.equals(comment.getAuthor().getId(), principal.getId());
        boolean isAdmin = principal.isAdmin();

        //=== 만약 늦참에 의해 생성된 댓글이라면 별점도 회수 ===//
        EpisodeStar episodeStar = comment.getEpisodeStar();
        if (episodeStar != null && episodeStar.isLateParticipating()) {
            episodeStar.withdrawScore();
        }

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
