package com.duckstar.converter;

import com.duckstar.domain.mapping.WeekAnime;
import com.duckstar.web.dto.SummaryDto.RankPreviewDto;

import java.util.List;

public class AnimeConverter {
    public static List<RankPreviewDto> toAnimeRankPreviewDtos(List<WeekAnime> weekAnimes) {
        return weekAnimes.stream()
                .map(RankPreviewDto::from)
                .toList();
    }
}
