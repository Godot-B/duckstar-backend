package com.duckstar.service;

import com.duckstar.abroad.aniLab.AnilabRepository;
import com.duckstar.abroad.animeCorner.AnimeCornerRepository;
import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.QuarterHandler;
import com.duckstar.apiPayload.exception.handler.WeekHandler;
import com.duckstar.domain.Quarter;
import com.duckstar.domain.Season;
import com.duckstar.domain.Week;
import com.duckstar.domain.enums.DayOfWeekShort;
import com.duckstar.domain.enums.SeasonType;
import com.duckstar.domain.enums.VoteStatus;
import com.duckstar.repository.AnimeSeason.AnimeSeasonRepository;
import com.duckstar.repository.Episode.EpisodeRepository;
import com.duckstar.repository.SeasonRepository;
import com.duckstar.repository.Week.WeekRepository;
import com.duckstar.web.dto.PageInfo;
import com.duckstar.web.dto.RankInfoDto.RankPreviewDto;
import com.duckstar.web.dto.WeekResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import static com.duckstar.util.QuarterUtil.*;
import static com.duckstar.web.dto.AnimeResponseDto.*;
import static com.duckstar.web.dto.ChartDto.*;
import static com.duckstar.web.dto.SearchResponseDto.*;
import static com.duckstar.web.dto.WeekResponseDto.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeekService {

    private final WeekRepository weekRepository;
    private final AnimeSeasonRepository animeSeasonRepository;
    private final SeasonRepository seasonRepository;
    private final EpisodeRepository episodeRepository;

    private final SeasonService seasonService;
    private final AnilabRepository anilabRepository;
    private final AnimeCornerRepository animeCornerRepository;

    public Week getCurrentWeek() {
        LocalDateTime now = LocalDateTime.now();
        return getWeekByTime(now);
    }

    public Week getWeekByTime(LocalDateTime time) {
        return weekRepository.findWeekByStartDateTimeLessThanEqualAndEndDateTimeGreaterThan(time, time)
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

    public Map<Integer, List<SeasonType>> getSeasons() {
        return seasonRepository.findAll().stream()
                .filter(Season::getIsPrepared)
                .sorted(Comparator.comparing(Season::getYearValue).reversed()
                        .thenComparing(Season::getTypeOrder).reversed())
                .collect(Collectors.groupingBy(
                        Season::getYearValue,
                        LinkedHashMap::new, // 순서 보장
                        Collectors.mapping(Season::getType, Collectors.toList())
                ));
    }

    public AnimePreviewListDto getWeeklyScheduleFromOffset(LocalTime offset) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastMonday = now
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .withHour(offset.getHour()).withMinute(offset.getMinute())
                .withSecond(0).withNano(0);
        LocalDateTime endMonday = lastMonday.plusDays(7);

        List<AnimePreviewDto> animePreviews =
                episodeRepository.getAnimePreviewsByDuration(
                        lastMonday,
                        endMonday
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

        Week week = getWeekByTime(now);
        return AnimePreviewListDto.builder()
                .schedule(schedule)
                .build();
    }

    public AnimePreviewListDto getScheduleByQuarterId(Integer year, Integer quarter) {
        Long quarterId = getQuarterIdByYQ(year, quarter);
        List<AnimePreviewDto> animePreviews =
                animeSeasonRepository.getAnimePreviewsByQuarter(quarterId);

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
                .year(year)
                .quarter(quarter)
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

        Week week = weekRepository.findById(weekId)
                .orElseThrow(() -> new WeekHandler(ErrorStatus.WEEK_NOT_FOUND));

        if (!week.getAnnouncePrepared()) throw new WeekHandler(ErrorStatus.ANNOUNCEMENT_NOT_PREPARED);

        List<AnimeRankDto> rows =
                episodeRepository.getAnimeRankDtosByWeekIdWithOverFetch(weekId, overFetch);
        boolean duckstarHasNext = rows.size() > size;

        List<RankPreviewDto> animeCornerRankDtos =
                animeCornerRepository.findAllByWeek_IdWithOverFetch(weekId, overFetch).stream()
                        .map(RankPreviewDto::of)
                        .toList();
        boolean animeCornerHasNext = animeCornerRankDtos.size() > size;

        List<RankPreviewDto> aniLabRankDtos =
                anilabRepository.findAllByWeek_IdWithOverFetch(weekId, overFetch).stream()
                        .map(RankPreviewDto::of)
                        .toList();
        boolean aniLabHasNext = aniLabRankDtos.size() > size;

        boolean hasNextTotal = duckstarHasNext || animeCornerHasNext || aniLabHasNext;

        if (hasNextTotal) {
            if (duckstarHasNext) rows = rows.subList(0, size);
            if (animeCornerHasNext) animeCornerRankDtos = animeCornerRankDtos.subList(0, size);
            if (aniLabHasNext) aniLabRankDtos = aniLabRankDtos.subList(0, size);
        }

        PageInfo pageInfo = PageInfo.builder()
                .hasNext(hasNextTotal)
                .page(page)
                .size(size)
                .build();

        return AnimeRankSliceDto.builder()
                .animeRankDtos(rows)
                .animeTrendRankPreviews(animeCornerRankDtos)
                .aniLabRankPreviews(aniLabRankDtos)
                .pageInfo(pageInfo)
                .build();
    }

    @Transactional
    public void setupWeeklyVote(Long lastWeekId, LocalDateTime now, YQWRecord record) {
        Week lastWeek = weekRepository.findWeekById(lastWeekId).orElseThrow(() ->
                new WeekHandler(ErrorStatus.WEEK_NOT_FOUND));
        int lastWeekQuarterValue = lastWeek.getQuarter().getQuarterValue();

        //=== 지난 투표 주 마감 ===//
        lastWeek.closeVote();

        int thisQuarterValue = record.quarterValue();

        //=== 분기, 시즌 찾기(or 생성) & 주 생성 ===//
        boolean isQuarterChanged = lastWeekQuarterValue != thisQuarterValue;

        Quarter quarter = seasonService.getOrCreateQuarter(isQuarterChanged, record.yearValue(), thisQuarterValue);
        Season season = seasonService.getOrCreateSeason(isQuarterChanged, quarter);

        Week newWeek = Week.create(
                quarter,
                record.weekValue(),
                now
        );
        Week savedWeek = weekRepository.save(newWeek);

        //=== 애니 후보군 생성 ===//
//        List<Anime> nowShowingAnimes = animeService.getAnimesForCandidate(season, now);
//
//        List<AnimeCandidate> animeCandidates = nowShowingAnimes.stream()
//                .map(anime -> AnimeCandidate.create(savedWeek, anime))
//                .toList();
//        animeCandidateRepository.saveAll(animeCandidates);

        // TODO 캐릭터 후보군 생성

        //=== 새로운 주의 투표 오픈 ===//
        newWeek.openVote();
    }

    public List<WeekDto> getAllWeeks() {
        return weekRepository.findAll().stream()
                .filter(week -> week.getStatus() == VoteStatus.CLOSED && week.getAnnouncePrepared())
                .sorted(Comparator.comparing(Week::getStartDateTime))
                .map(WeekDto::of)
                .toList();
    }
}
