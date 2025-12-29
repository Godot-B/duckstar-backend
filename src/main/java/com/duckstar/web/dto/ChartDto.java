package com.duckstar.web.dto;

import com.duckstar.web.dto.CharacterResponseDto.CharacterRankDto;
import com.duckstar.web.dto.RankInfoDto.RankPreviewDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

import static com.duckstar.web.dto.AnimeResponseDto.*;
import static com.duckstar.web.dto.RankInfoDto.*;
import static com.duckstar.web.dto.SurveyResponseDto.*;

public class ChartDto {

    @Builder
    @Getter
    public static class AnimeRankSliceDto {
        List<AnimeRankDto> animeRankDtos;

        List<RankPreviewDto> animeTrendRankPreviews;

        List<RankPreviewDto> aniLabRankPreviews;

        PageInfo pageInfo;
    }

    @Builder
    @Getter
    public static class SurveyRankSliceDto {
        // 첫 슬라이스에서만 서베이 정보 보내기
        @JsonInclude(JsonInclude.Include.NON_NULL)
        SurveyDto surveyDto;

        List<SurveyRankDto> surveyRankDtos;

        PageInfo pageInfo;
    }

    @Builder
    @Getter
    public static class CharacterRankSliceDto {
        List<CharacterRankDto> characterRankDtos;

        List<RankPreviewDto> abroadRankPreviews;

        PageInfo pageInfo;
    }
}
