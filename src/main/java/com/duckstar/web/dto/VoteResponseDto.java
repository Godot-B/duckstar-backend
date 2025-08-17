package com.duckstar.web.dto;

import com.duckstar.web.dto.WeekResponseDto.WeekDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class VoteResponseDto {

    @Builder
    @Getter
    public static class AnimeCandidateListDto {
        WeekDto weekDto;

        List<AnimeCandidateDto> animeCandidates;
    }

    @Builder
    @Getter
    public static class AnimeCandidateDto {
        Long animeCandidateId;

        String mainThumbnailUrl;

        String titleKor;
    }
}
