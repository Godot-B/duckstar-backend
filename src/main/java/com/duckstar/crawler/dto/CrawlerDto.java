package com.duckstar.crawler.dto;

import lombok.Builder;
import lombok.Getter;

public class CrawlerDto {

    @Builder
    @Getter
    public static class CrawlerRankDto {
        Integer rank;

        Integer rankDiff;

        Integer consecutiveWeeksAtSameRank;

        String thumbnailUrl;

        String mainTitle;

        String subTitle;
    }
}
