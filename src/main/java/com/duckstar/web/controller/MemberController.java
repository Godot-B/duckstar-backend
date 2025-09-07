package com.duckstar.web.controller;

import com.duckstar.apiPayload.ApiResponse;
import com.duckstar.security.MemberPrincipal;
import com.duckstar.service.MemberService;
import com.duckstar.web.dto.MemberRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @PatchMapping("/me/profile")
    public ApiResponse<UpdateReceiptDto> updateProfile(
            @Valid @ModelAttribute ProfileRequestDto request,
            @AuthenticationPrincipal MemberPrincipal principal) {
        return ApiResponse.onSuccess(
                memberService.updateProfile(request, principal));
    }
}
