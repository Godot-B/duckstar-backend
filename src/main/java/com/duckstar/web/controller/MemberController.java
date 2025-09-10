package com.duckstar.web.controller;

import com.duckstar.apiPayload.ApiResponse;
import com.duckstar.security.MemberPrincipal;
import com.duckstar.service.MemberService;
import com.duckstar.web.dto.MemberRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.duckstar.web.dto.MemberRequestDto.*;
import static com.duckstar.web.dto.MemberResponseDto.*;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/me")
    public MePreviewDto getCurrentUser(
            @AuthenticationPrincipal MemberPrincipal principal) {
        if (principal == null) {
            return MePreviewDto.ofEmpty();
        } else {
            return memberService.getCurrentUser(principal.getId());
        }
    }

    @Operation(summary = "프로필 수정 API", description = "닉네임 또는 프로필 이미지를 수정합니다.")
    @PatchMapping(value = "/me/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UpdateReceiptDto> updateProfile(
            @ModelAttribute ProfileRequestDto request,
            @AuthenticationPrincipal MemberPrincipal principal) {
        return ApiResponse.onSuccess(
                memberService.updateProfile(request, principal));
    }
}
