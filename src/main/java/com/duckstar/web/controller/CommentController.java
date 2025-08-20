package com.duckstar.web.controller;

import com.duckstar.apiPayload.ApiResponse;
import com.duckstar.security.MemberPrincipal;
import com.duckstar.service.CommentService;
import com.duckstar.web.dto.CommentResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.duckstar.web.dto.CommentResponseDto.*;

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

    @Operation(summary = "답글 달기 API")
    @PostMapping("/{commentId}")
    public ApiResponse<ReplyDto> leaveReply(
            @PathVariable Long commentId,
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        return ApiResponse.onSuccess(
                commentService.leaveReply(commentId, principal));
    }
}
