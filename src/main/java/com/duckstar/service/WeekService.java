package com.duckstar.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.WeekHandler;
import com.duckstar.repository.AnimeWeek.WeekAnimeRepository;
import com.duckstar.repository.Week.WeekRepository;
import com.duckstar.web.dto.AnimeResponseDto.AnimeRankDto;
import com.duckstar.web.dto.ChartDto.AnimeRankSliceDto;
import com.duckstar.web.dto.SummaryDto;
import com.duckstar.web.dto.SummaryDto.RankSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.duckstar.web.dto.ChartDto.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeekService {

    private final WeekRepository weekRepository;
    private final WeekAnimeRepository weekAnimeRepository;

    public Long getWeekIdByYQW(Integer year, Integer quarter, Integer week) {
        return weekRepository.findWeekIdByYQW(year, quarter, week)
                .orElseThrow(() -> new WeekHandler(ErrorStatus.WEEK_NOT_FOUND));
    }

    public AnimeRankSliceDto getAnimeRankSliceDto(Long weekId, Pageable pageable) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();

        Pageable overFetch = PageRequest.of(
                page,
                size + 1,
                pageable.getSort()
        );

        List<AnimeRankDto> rows =
                weekAnimeRepository.getAnimeRankDtosByWeekId(weekId, overFetch);
        boolean leftHasNext = rows.size() > size;

        List<RankSummaryDto> animeTrendRankDtos = null;
        boolean animeTrendHasNext = false;

        List<RankSummaryDto> aniLabRankDtos = null;
        boolean aniLabHasNext = false;

        boolean hasNextTotal = leftHasNext || animeTrendHasNext || aniLabHasNext;

        if (hasNextTotal) rows = rows.subList(0, size);

        PageInfo pageInfo = PageInfo.builder()
                .hasNext(hasNextTotal)
                .page(page)
                .size(size)
                .build();

        return AnimeRankSliceDto.builder()
                .animeRankDtos(rows)
                .animeTrendRankDtos(null)
                .aniLabRankDtos(null)
                .pageInfo(pageInfo)
                .build();
    }
}
