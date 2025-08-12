package com.duckstar.web.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CardDto {

    Integer rank;

    Integer rankDiff;

    Integer consecutiveWeeksAtSameRank;

    String thumbnailUrl;

    String title;

    String subTitle;
}
