package com.duckstar.web.controller;

import com.duckstar.apiPayload.ApiResponse;
import com.duckstar.security.MemberPrincipal;
import com.duckstar.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.duckstar.web.dto.CommentResponseDto.*;
import static com.duckstar.web.dto.WriteRequestDto.*;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "애니메이션 댓글 삭제 API")
    @PatchMapping("/{commentId}")
    public ApiResponse<DeleteResultDto> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        return ApiResponse.onSuccess(
                commentService.deleteAnimeComment(commentId, principal));
    }

    @Operation(summary = "답글 작성 API")
    @PostMapping("/{commentId}/replies")
    public ApiResponse<ReplyDto> leaveReply(
            @PathVariable Long commentId,
            @Valid @RequestBody ReplyRequestDto request,
            @AuthenticationPrincipal(expression = "id") Long memberId
    ) {
        return ApiResponse.onSuccess(
                commentService.leaveReply(
                        commentId,
                        request,
                        memberId
                )
        );
    }

    @Operation(summary = "답글 조회 API")
    @GetMapping("/{commentId}/replies")
    public ApiResponse<ReplySliceDto> getReplies(
            @PathVariable Long commentId,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        return ApiResponse.onSuccess(
                commentService.getReplySliceDto(
                        commentId,
                        pageable,
                        principal
                )
        );
    }

    @Operation(summary = "답글 삭제 API")
    @PatchMapping("/{commentId}/replies/{replyId}")
    public ApiResponse<DeleteResultDto> deleteReply(
            @PathVariable Long replyId,
            @AuthenticationPrincipal MemberPrincipal principal
    ) {

        return ApiResponse.onSuccess(
                commentService.deleteReply(replyId, principal));
    }
}
