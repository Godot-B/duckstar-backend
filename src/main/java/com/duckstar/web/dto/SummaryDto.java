package com.duckstar.web.dto;

import lombok.Builder;
import lombok.Getter;

public class SummaryDto {

    @Builder
    @Getter
    public static class RankSummaryDto {
        Integer rank;

        Integer rankDiff;

        Integer consecutiveWeeksAtSameRank;

        String mainThumbnailUrl;

        String title;

        String subTitle;
    }

    @Builder
    @Getter
    public static class RankPreviewDto {
        Double votePercent;

        RankSummaryDto rankSummaryDto;
    }
}
