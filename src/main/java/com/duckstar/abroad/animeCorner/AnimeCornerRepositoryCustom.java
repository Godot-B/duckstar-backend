package com.duckstar.abroad.animeCorner;

import java.util.List;

public interface AnimeCornerRepositoryCustom {
    List<AnimeCorner> findAllByWeek_Id(Long weekId, int offset, int limit);
}
