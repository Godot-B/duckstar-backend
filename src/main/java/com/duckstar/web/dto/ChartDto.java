package com.duckstar.web.dto;

import com.duckstar.web.dto.CharacterResponseDto.CharacterRankDto;
import com.duckstar.web.dto.RankInfoDto.RankPreviewDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

import static com.duckstar.web.dto.AnimeResponseDto.*;
import static com.duckstar.web.dto.RankInfoDto.*;

public class ChartDto {

    @Builder
    @Getter
    public static class AnimeRankSliceDto {
        Integer voterCount;

        Integer voteTotalCount;

        List<AnimeRankDto> animeRankDtos;

        List<RankPreviewDto> animeTrendRankPreviews;

        List<RankPreviewDto> aniLabRankPreviews;

        PageInfo pageInfo;
    }

    @Builder
    @Getter
    public static class SurveyRankPage {
        Integer voteTotalCount;

        List<SurveyRankDto> surveyRankDtos;

        // offset 페이징
        private Integer page;          // 요청한 페이지 번호
        private Integer size;          // 요청한 페이지당 사이즈
        private Integer totalPages;    // 전체 페이지 수
        private Long totalElements;    // 전체 데이터 수
        private Boolean isFirst;       // 첫 페이지 여부
        private Boolean isLast;        // 마지막 페이지 여부
    }

    @Builder
    @Getter
    public static class CharacterRankSliceDto {
        List<CharacterRankDto> characterRankDtos;

        List<RankPreviewDto> abroadRankPreviews;

        PageInfo pageInfo;
    }
}
