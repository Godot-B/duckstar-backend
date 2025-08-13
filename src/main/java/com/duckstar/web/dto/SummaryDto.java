package com.duckstar.web.dto;

import com.duckstar.domain.Anime;
import com.duckstar.domain.RankInfo;
import com.duckstar.domain.mapping.WeekAnime;
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

        public static RankPreviewDto from(WeekAnime weekAnime) {
            RankInfo rankInfo = weekAnime.getRankInfo();
            Anime anime = weekAnime.getAnime();
            SummaryDto.RankSummaryDto rankSummaryDto = SummaryDto.RankSummaryDto.builder()
                    .rank(rankInfo.getRank())
                    .rankDiff(rankInfo.getRankDiff())
                    .consecutiveWeeksAtSameRank(rankInfo.getConsecutiveWeeksAtSameRank())
                    .mainThumbnailUrl(anime.getMainThumbnailUrl())
                    .title(anime.getTitleKor())
                    .subTitle(anime.getCorp())
                    .build();

            return RankPreviewDto.builder()
                    .votePercent(rankInfo.getVotePercent())
                    .rankSummaryDto(rankSummaryDto)
                    .build();
        }


//        public static RankPreviewDto from(WeekCharacter weekAnime) {
//
//        }
    }
}
