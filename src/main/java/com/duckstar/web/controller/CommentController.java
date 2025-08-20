package com.duckstar.web.controller;

import com.duckstar.apiPayload.ApiResponse;
import com.duckstar.security.MemberPrincipal;
import com.duckstar.service.CommentService;
import com.duckstar.web.dto.CommentResponseDto;
import com.duckstar.web.dto.WriteRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
            @AuthenticationPrincipal(expression = "id") Long authorId
    ) {
        return ApiResponse.onSuccess(
                commentService.leaveReply(commentId, request, authorId));
    }
}
