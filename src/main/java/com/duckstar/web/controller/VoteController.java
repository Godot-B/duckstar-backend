package com.duckstar.web.controller;

import com.duckstar.apiPayload.ApiResponse;
import com.duckstar.service.VoteService;
import com.duckstar.web.dto.VoteResponseDto;
import com.duckstar.web.dto.VoteResponseDto.AnimeCandidateListDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vote")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    @Operation(summary = "애니메이션 후보자 리스트 조회 API")
    @GetMapping("/anime")
    public ApiResponse<AnimeCandidateListDto> voteAnime() {
        return ApiResponse.onSuccess(
                voteService.getAnimeCandidateList());
    }


}
