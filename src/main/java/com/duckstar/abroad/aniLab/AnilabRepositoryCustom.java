package com.duckstar.abroad.aniLab;

import java.util.List;

public interface AnilabRepositoryCustom {
    List<Anilab> findAllByWeek_Id(Long weekId, int offset, int limit);
}
