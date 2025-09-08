package com.duckstar.web.controller;

import com.duckstar.apiPayload.ApiResponse;
import com.duckstar.security.MemberPrincipal;
import com.duckstar.service.ReplyService;
import com.duckstar.web.dto.BoardRequestDto;
import com.duckstar.web.dto.CommentResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.duckstar.web.dto.BoardRequestDto.*;
import static com.duckstar.web.dto.CommentResponseDto.*;

@RestController
@RequestMapping("/api/v1/replies")
@RequiredArgsConstructor
public class ReplyController {

    private final ReplyService replyService;

    @Operation(summary = "답글 삭제 API")
    @PatchMapping("/{replyId}")
    public ApiResponse<DeleteResultDto> deleteReply(
            @PathVariable Long replyId,
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        return ApiResponse.onSuccess(
                replyService.deleteReply(replyId, principal));
    }

    @Operation(summary = "답글 좋아요 API")
    @PostMapping("/{replyId}/like")
    public ApiResponse<LikeResultDto> likeReply(
            @PathVariable Long replyId,
            @RequestBody LikeRequestDto request,
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        return ApiResponse.onSuccess(
                replyService.giveLike(
                        replyId,
                        request.getLikeId(),
                        principal
                )
        );
    }

    @Operation(summary = "답글 좋아요 취소 API")
    @PatchMapping("/{replyId}/like/{replyLikeId}")
    public ApiResponse<DiscardLikeResultDto> dislikeReply(
            @PathVariable Long replyId,
            @PathVariable Long replyLikeId,
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        return ApiResponse.onSuccess(
                replyService.discardLike(
                        replyId,
                        replyLikeId,
                        principal
                )
        );
    }
}
