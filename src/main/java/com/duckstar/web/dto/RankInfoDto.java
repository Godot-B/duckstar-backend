package com.duckstar.web.dto;

import com.duckstar.abroad.aniLab.Anilab;
import com.duckstar.abroad.animeCorner.AnimeCorner;
import com.duckstar.domain.Anime;
import com.duckstar.domain.enums.ContentType;
import com.duckstar.domain.mapping.weeklyVote.Episode;
import com.duckstar.domain.vo.RankInfo;
import lombok.Builder;
import lombok.Getter;

public class RankInfoDto {

    @Builder
    @Getter
    public static class RankPreviewDto {
        ContentType type;

        Long contentId;

        Integer rank;

        Integer rankDiff;

        Integer consecutiveWeeksAtSameRank;

        String mainThumbnailUrl;

        String title;

        String subTitle;

        public static RankPreviewDto of(Episode episode) {
            if (episode == null) {
                return RankPreviewDto.builder().build();
            }

            RankInfo rankInfo = episode.getRankInfo();
            Anime anime = episode.getAnime();

            return RankPreviewDto.builder()
                    .type(ContentType.ANIME)
                    .contentId(anime.getId())
                    .rank(rankInfo != null ? rankInfo.getRank() : null)
                    .rankDiff(rankInfo != null ? rankInfo.getRankDiff() : null)
                    .consecutiveWeeksAtSameRank(rankInfo != null ? rankInfo.getConsecutiveWeeksAtSameRank() : null)
                    .mainThumbnailUrl(anime.getMainThumbnailUrl())
                    .title(anime.getTitleKor())
                    .subTitle(anime.getCorp())
                    .build();
        }

        public static RankPreviewDto of(AnimeCorner animeCorner) {
            Anime anime = animeCorner.getAnime();

            return RankPreviewDto.builder()
                    .type(ContentType.ANIME)
                    .contentId(anime != null ? anime.getId() : null)
                    .rank(animeCorner.getRank())
                    .rankDiff(animeCorner.getRankDiff())
                    .consecutiveWeeksAtSameRank(animeCorner.getConsecutiveWeeksAtSameRank())
                    .mainThumbnailUrl(
                            anime != null ?
                                    anime.getMainThumbnailUrl() :
                                    animeCorner.getMainThumbnailUrl()
                    )
                    .title(animeCorner.getTitle())
                    .subTitle(anime != null ? anime.getCorp() : null)
                    .build();
        }

        public static RankPreviewDto of(Anilab anilab) {
            Anime anime = anilab.getAnime();

            return RankPreviewDto.builder()
                    .type(ContentType.ANIME)
                    .contentId(
                            anime != null ?
                                    anime.getId() :
                                    null
                    )
                    .rank(anilab.getRank())
                    .rankDiff(anilab.getRankDiff())
                    .consecutiveWeeksAtSameRank(anilab.getConsecutiveWeeksAtSameRank())
                    .mainThumbnailUrl(
                            anime != null ?
                                    anime.getMainThumbnailUrl() :
                                    anilab.getMainThumbnailUrl()
                    )
                    .title(anilab.getTitle())
                    .subTitle(
                            anime != null ?
                                    anime.getCorp() :
                                    null
                    )
                    .build();
        }
    }

    @Builder
    @Getter
    public static class DuckstarRankPreviewDto {
        Double averageRating;
        Integer voterCount;

        RankPreviewDto rankPreviewDto;

        public static DuckstarRankPreviewDto of(Episode episode) {
            RankInfo rankInfo = episode.getRankInfo();
            Anime anime = episode.getAnime();
            RankPreviewDto rankPreviewDto = RankPreviewDto.builder()
                    .type(ContentType.ANIME)
                    .contentId(anime.getId())
                    .rank(rankInfo != null ? rankInfo.getRank() : null)
                    .rankDiff(rankInfo != null ? rankInfo.getRankDiff() : null)
                    .consecutiveWeeksAtSameRank(rankInfo != null ? rankInfo.getConsecutiveWeeksAtSameRank() : null)
                    .mainThumbnailUrl(anime.getMainThumbnailUrl())
                    .title(anime.getTitleKor())
                    .subTitle(anime.getCorp())
                    .build();

            return DuckstarRankPreviewDto.builder()
                    .averageRating(rankInfo != null ? rankInfo.getRankedAverage() : null)
                    .voterCount(rankInfo != null ? rankInfo.getRankedVoterCount() : null)
                    .rankPreviewDto(rankPreviewDto)
                    .build();
        }

//        public static DuckstarRankPreviewDto of(AnimeCandidate animeCandidate) {
//            com.duckstar.domain.mapping.legacy_vote.RankInfo rankInfo = animeCandidate.getRankInfo();
//            Anime anime = animeCandidate.getAnime();
//            RankPreviewDto rankPreviewDto = RankPreviewDto.builder()
//                    .type(ContentType.ANIME)
//                    .contentId(anime.getId())
//                    .rank(rankInfo != null ? rankInfo.getRank() : null)
//                    .rankDiff(rankInfo != null ? rankInfo.getRankDiff() : null)
//                    .consecutiveWeeksAtSameRank(rankInfo != null ? rankInfo.getConsecutiveWeeksAtSameRank() : null)
//                    .mainThumbnailUrl(anime.getMainThumbnailUrl())
//                    .title(anime.getTitleKor())
//                    .subTitle(anime.getCorp())
//                    .build();
//
//            return DuckstarRankPreviewDto.builder()
////                    .votePercent(rankInfo != null ? rankInfo.getVotePercent() : null)
//                    .rankPreviewDto(rankPreviewDto)
//                    .build();
//        }
    }

    @Builder
    @Getter
    public static class VoteRatioDto {
        Double votePercent;

        Double malePercent;

        Double femalePercent;
    }
}
