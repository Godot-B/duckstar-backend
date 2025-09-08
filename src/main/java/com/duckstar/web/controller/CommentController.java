package com.duckstar.web.controller;

import com.duckstar.apiPayload.ApiResponse;
import com.duckstar.security.MemberPrincipal;
import com.duckstar.service.CommentService;
import com.duckstar.service.ReplyService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.duckstar.web.dto.CommentResponseDto.*;
import static com.duckstar.web.dto.BoardRequestDto.*;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final ReplyService replyService;

    @Operation(summary = "답글 조회 API")
    @GetMapping("/{commentId}/replies")
    public ApiResponse<ReplySliceDto> getReplies(
            @PathVariable Long commentId,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        return ApiResponse.onSuccess(
                replyService.getReplySliceDto(
                        commentId,
                        pageable,
                        principal
                )
        );
    }

    @Operation(summary = "댓글 삭제 API")
    @PatchMapping("/{commentId}")
    public ApiResponse<DeleteResultDto> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        return ApiResponse.onSuccess(
                commentService.deleteAnimeComment(commentId, principal));
    }

    @Operation(summary = "댓글 좋아요 API")
    @PostMapping("/{commentId}/like")
    public ApiResponse<LikeResultDto> likeComment(
            @PathVariable Long commentId,
            @RequestBody LikeRequestDto request,
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        return ApiResponse.onSuccess(
                commentService.giveLike(
                        commentId,
                        request.getLikeId(),
                        principal
                )
        );
    }

    @Operation(summary = "댓글 좋아요 취소 API")
    @PatchMapping("/{commentId}/like/{commentLikeId}")
    public ApiResponse<DiscardLikeResultDto> dislikeComment(
            @PathVariable Long commentId,
            @PathVariable Long commentLikeId,
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        return ApiResponse.onSuccess(
                commentService.discardLike(
                        commentId,
                        commentLikeId,
                        principal
                )
        );
    }

    @Operation(summary = "답글 작성 API")
    @PostMapping("/{commentId}/replies")
    public ApiResponse<ReplyDto> leaveReply(
            @PathVariable Long commentId,
            @Valid @ModelAttribute ReplyRequestDto request,
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        return ApiResponse.onSuccess(
                replyService.leaveReply(
                        commentId,
                        request,
                        principal
                )
        );
    }
}
