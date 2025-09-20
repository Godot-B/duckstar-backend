package com.duckstar.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.QuarterHandler;
import com.duckstar.apiPayload.exception.handler.WeekHandler;
import com.duckstar.domain.Anime;
import com.duckstar.domain.Quarter;
import com.duckstar.domain.Season;
import com.duckstar.domain.Week;
import com.duckstar.domain.enums.DayOfWeekShort;
import com.duckstar.domain.enums.Gender;
import com.duckstar.domain.mapping.AnimeCandidate;
import com.duckstar.domain.mapping.AnimeVote;
import com.duckstar.domain.vo.RankInfo;
import com.duckstar.repository.AnimeCandidate.AnimeCandidateRepository;
import com.duckstar.repository.AnimeSeason.AnimeSeasonRepository;
import com.duckstar.repository.AnimeVote.AnimeVoteRepository;
import com.duckstar.repository.QuarterRepository;
import com.duckstar.repository.SeasonRepository;
import com.duckstar.repository.Week.WeekRepository;
import com.duckstar.web.dto.AnimeResponseDto.AnimeRankDto;
import com.duckstar.web.dto.ChartDto.AnimeRankSliceDto;
import com.duckstar.web.dto.PageInfo;
import com.duckstar.web.dto.RankInfoDto.RankPreviewDto;
import com.duckstar.web.dto.WeekResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.duckstar.util.QuarterUtil.*;
import static com.duckstar.web.dto.SearchResponseDto.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeekService {

    private final WeekRepository weekRepository;
    private final AnimeCandidateRepository animeCandidateRepository;
    private final QuarterRepository quarterRepository;
    private final AnimeSeasonRepository animeSeasonRepository;
    private final SeasonRepository seasonRepository;
    private final AnimeService animeService;

    public Week getCurrentWeek() {
        LocalDateTime now = LocalDateTime.now();

        return weekRepository.findWeekByStartDateTimeLessThanEqualAndEndDateTimeGreaterThan(now, now)
                .orElseThrow(() -> new WeekHandler(ErrorStatus.WEEK_NOT_FOUND));
    }

    public Long getQuarterIdByYQ(Integer year, Integer quarter) {
        return weekRepository.findQuarterIdByYQ(year, quarter)
                .orElseThrow(() -> new QuarterHandler(ErrorStatus.QUARTER_NOT_FOUND));
    }

    public Long getWeekIdByYQW(Integer year, Integer quarter, Integer week) {
        return weekRepository.findWeekIdByYQW(year, quarter, week)
                .orElseThrow(() -> new WeekHandler(ErrorStatus.WEEK_NOT_FOUND));
    }

    public AnimePreviewListDto getScheduleByQuarterId(Long quarterId) {
        Week currentWeek = getCurrentWeek();
        LocalDateTime weekStart = currentWeek.getStartDateTime();
        LocalDateTime weekEnd = currentWeek.getEndDateTime();

        List<AnimePreviewDto> animePreviews =
                animeSeasonRepository.getSeasonAnimePreviewsByQuarterAndWeek(
                        quarterId,
                        weekStart,
                        weekEnd
                );

        Map<DayOfWeekShort, List<AnimePreviewDto>> schedule = animePreviews.stream()
                .collect(
                        Collectors.groupingBy(dto -> {
                            DayOfWeekShort dayOfWeek = dto.getDayOfWeek();
                            return (dto.getDayOfWeek() != null) ?
                                    dayOfWeek :
                                    DayOfWeekShort.NONE;
                        })
                );

        DayOfWeekShort[] keys = DayOfWeekShort.values();
        for (DayOfWeekShort key : keys) {
            schedule.putIfAbsent(key, List.of());
        }

        return AnimePreviewListDto.builder()
                .weekDto(WeekResponseDto.WeekDto.from(currentWeek))
                .schedule(schedule)
                .build();
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
                animeCandidateRepository.getAnimeRankDtosByWeekId(weekId, overFetch);
        boolean duckstarHasNext = rows.size() > size;

        List<RankPreviewDto> animeTrendRankDtos = null;
        boolean animeTrendHasNext = false;

        List<RankPreviewDto> aniLabRankDtos = null;
        boolean aniLabHasNext = false;

        boolean hasNextTotal = duckstarHasNext || animeTrendHasNext || aniLabHasNext;

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

    @Transactional
    public void setupWeeklyVote(LocalDateTime now, Long lastWeekId, YQWRecord record) {
        Week lastWeek = weekRepository.findWeekById(lastWeekId).orElseThrow(() ->
                new WeekHandler(ErrorStatus.WEEK_NOT_FOUND));
        int lastWeekQuarterValue = lastWeek.getQuarter().getQuarterValue();

        //=== 지난 투표 주 마감 ===//
        lastWeek.closeVote();

        int thisQuarterValue = record.quarterValue();

        //=== 분기, 시즌 찾기(or 생성) & 주 생성 ===//
        boolean isQuarterChanged = lastWeekQuarterValue != thisQuarterValue;

        Quarter quarter = getOrCreateQuarter(isQuarterChanged, record);
        Season season = getOrCreateSeason(isQuarterChanged, quarter);

        Week newWeek = Week.create(
                quarter,
                record.weekValue(),
                now
        );
        weekRepository.save(newWeek);

        //=== 애니 후보군 생성 ===//
        List<Anime> nowShowingAnimes = animeService.updateAndGetAnimes(now, isQuarterChanged, season);

        List<AnimeCandidate> animeCandidates = nowShowingAnimes.stream()
                .map(anime -> AnimeCandidate.create(newWeek, anime))
                .toList();
        animeCandidateRepository.saveAll(animeCandidates);

        // TODO 캐릭터 후보군 생성

        //=== 새로운 주의 투표 오픈 ===//
        newWeek.openVote();
    }


    // CSV 리더 설계 이후 다시 돌아와 아래 로직들 검증

    private Quarter getOrCreateQuarter(boolean isQuarterChanged, YQWRecord record) {
        int thisYearValue = record.yearValue();
        int thisQuarterValue = record.quarterValue();
        int thisWeekValue = record.weekValue();

        Optional<Quarter> quarterOpt =
                quarterRepository.findByYearValueAndQuarterValue(thisYearValue, thisQuarterValue);

        Quarter quarter;
        // 분기 변경 주 && DB에 없을 때
        if (isQuarterChanged && quarterOpt.isEmpty()) {
            quarter = Quarter.create(thisQuarterValue, thisWeekValue);
            quarterRepository.save(quarter);
        }
        quarter = quarterOpt.orElseThrow(() -> new QuarterHandler(ErrorStatus.QUARTER_NOT_FOUND));
        return quarter;
    }

    private Season getOrCreateSeason(boolean isChangedQuarter, Quarter quarter) {
        int thisYearValue = quarter.getYearValue();
        int thisQuarterValue = quarter.getQuarterValue();

        Optional<Season> seasonOpt =
                seasonRepository.findByYearValueAndQuarter_QuarterValue(thisYearValue, thisQuarterValue);
        Season season;
        if (isChangedQuarter && seasonOpt.isEmpty()) {
            season = Season.create(quarter, thisYearValue);
            seasonRepository.save(season);
        } else {
            season = seasonOpt.get();
        }
        return season;
    }
}
