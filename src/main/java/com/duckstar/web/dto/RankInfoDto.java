package com.duckstar.web.dto;

import com.duckstar.domain.Anime;
import com.duckstar.domain.vo.RankInfo;
import com.duckstar.domain.mapping.AnimeCandidate;
import lombok.Builder;
import lombok.Getter;

public class RankInfoDto {

    @Builder
    @Getter
    public static class RankPreviewDto {
        Integer rank;

        Integer rankDiff;

        Integer consecutiveWeeksAtSameRank;

        String mainThumbnailUrl;

        String title;

        String subTitle;
    }

    @Builder
    @Getter
    public static class DuckstarRankPreviewDto {
        Double votePercent;

        RankPreviewDto rankPreviewDto;

        public static DuckstarRankPreviewDto from(AnimeCandidate animeCandidate) {
            RankInfo rankInfo = animeCandidate.getRankInfo();
            Anime anime = animeCandidate.getAnime();
            RankPreviewDto rankPreviewDto = RankPreviewDto.builder()
                    .rank(rankInfo.getRank())
                    .rankDiff(rankInfo.getRankDiff())
                    .consecutiveWeeksAtSameRank(rankInfo.getConsecutiveWeeksAtSameRank())
                    .mainThumbnailUrl(anime.getMainThumbnailUrl())
                    .title(anime.getTitleKor())
                    .subTitle(anime.getCorp())
                    .build();

            return DuckstarRankPreviewDto.builder()
                    .votePercent(rankInfo.getVotePercent())
                    .rankPreviewDto(rankPreviewDto)
                    .build();
        }
    }

    @Builder
    @Getter
    public static class VoteRatioDto {
        Double votePercent;

        Double malePercent;

        Double femalePercent;
    }
}
