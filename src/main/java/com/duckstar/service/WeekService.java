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
import com.duckstar.domain.enums.Medium;
import com.duckstar.repository.AnimeSeason.AnimeSeasonRepository;
import com.duckstar.repository.Episode.EpisodeRepository;
import com.duckstar.repository.SeasonRepository;
import com.duckstar.repository.Week.WeekRepository;
import com.duckstar.web.dto.PageInfo;
import com.duckstar.web.dto.RankInfoDto.RankPreviewDto;
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

    public List<SeasonResponseDto> getSeasons() {
        return seasonRepository.findAll().stream()
                .filter(Season::getIsPrepared)
                .sorted(
                        Comparator.comparing(Season::getYearValue).reversed()
                                .thenComparing(Season::getTypeOrder, Comparator.reverseOrder())
                )
                .collect(Collectors.groupingBy(
                        Season::getYearValue,
                        LinkedHashMap::new,
                        Collectors.mapping(Season::getType, Collectors.toList())
                ))
                .entrySet().stream()
                .map(e -> SeasonResponseDto.builder()
                        .year(e.getKey())
                        .types(e.getValue())
                        .build()
                )
                .toList();
    }

    public AnimePreviewListDto getWeeklyScheduleFromOffset(LocalTime offset) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thisMonday = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .with(offset);
        LocalDateTime endMonday = thisMonday.plusDays(7);

        List<AnimePreviewDto> animePreviews =
                episodeRepository.getAnimePreviewsByDuration(thisMonday, endMonday);

        // 임시 그룹핑
        Map<DayOfWeekShort, List<AnimePreviewDto>> groupedMap = animePreviews.stream()
                .collect(Collectors.groupingBy(
                        dto -> dto.getDayOfWeek() == null ?
                                DayOfWeekShort.SPECIAL :
                                dto.getDayOfWeek()
                ));

        Comparator<AnimePreviewDto> scheduleComparator = (a, b) -> {
            int hourA = DayOfWeekShort.getLogicalHour(a.getScheduledAt().toLocalTime());
            int hourB = DayOfWeekShort.getLogicalHour(b.getScheduledAt().toLocalTime());

            if (hourA != hourB) return Integer.compare(hourA, hourB);

            // 시간이 같으면 분 단위 비교
            return a.getScheduledAt().getMinute() - b.getScheduledAt().getMinute();
        };

        // 모든 요일(MON~SPECIAL)을 순회하며 DTO 구성 및 내부 정렬
        List<ScheduleDto> scheduleDtos = Arrays.stream(DayOfWeekShort.values())
                .map(day -> {
                    List<AnimePreviewDto> list = new ArrayList<>(groupedMap.getOrDefault(day, List.of()));

                    // 서비스 단의 비즈니스 규칙(24시 정책)으로 정렬 수행
                    list.sort(scheduleComparator);

                    return ScheduleDto.builder()
                            .dayOfWeekShort(day)
                            .animePreviews(list)
                            .build();
                })
                .toList();

        return AnimePreviewListDto.builder()
                .scheduleDtos(scheduleDtos)
                .build();
    }

    public AnimePreviewListDto getScheduleByQuarterId(Integer year, Integer quarter) {
        Long quarterId = getQuarterIdByYQ(year, quarter);
        List<AnimePreviewDto> animePreviews =
                animeSeasonRepository.getAnimePreviewsByQuarter(quarterId);

        // 임시 그룹핑
        Map<DayOfWeekShort, List<AnimePreviewDto>> groupedMap = animePreviews.stream()
                .collect(Collectors.groupingBy(
                        dto -> dto.getDayOfWeek() == null ?
                                DayOfWeekShort.SPECIAL :
                                dto.getDayOfWeek()
                ));

        Comparator<AnimePreviewDto> scheduleComparator = (a, b) -> {
            LocalDateTime aScheduledAt = a.getScheduledAt();
            LocalTime aLocalTime = aScheduledAt == null ? a.getAirTime() : aScheduledAt.toLocalTime();
            LocalDateTime bScheduledAt = b.getScheduledAt();
            LocalTime bLocalTime = bScheduledAt == null ? b.getAirTime() : bScheduledAt.toLocalTime();

            int hourA = DayOfWeekShort.getLogicalHour(aLocalTime);
            int hourB = DayOfWeekShort.getLogicalHour(bLocalTime);

            if (hourA != hourB) return Integer.compare(hourA, hourB);

            // 시간이 같으면 분 단위 비교
            return aLocalTime.getMinute() - bLocalTime.getMinute();
        };

        // 모든 요일(MON~SPECIAL)을 순회하며 DTO 구성 및 내부 정렬
        List<ScheduleDto> scheduleDtos = Arrays.stream(DayOfWeekShort.values())
                .map(day -> {
                    List<AnimePreviewDto> list = new ArrayList<>(groupedMap.getOrDefault(day, List.of()));

                    // 서비스 단의 비즈니스 규칙(24시 정책)으로 정렬 수행
                    list.sort(scheduleComparator);

                    return ScheduleDto.builder()
                            .dayOfWeekShort(day)
                            .animePreviews(list)
                            .build();
                })
                .toList();

        return AnimePreviewListDto.builder()
                .year(year)
                .quarter(quarter)
                .scheduleDtos(scheduleDtos)
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

        LocalDateTime weekEndDateTime = week.getEndDateTime();
        List<AnimeRankDto> rows =
                episodeRepository.getAnimeRankDtosByWeekIdWithOverFetch(weekId, weekEndDateTime, overFetch);
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
                .voterCount(week.getAnimeVoterCount())
                .voteTotalCount(week.getAnimeVotes())
                .animeRankDtos(rows)
                .animeTrendRankPreviews(animeCornerRankDtos)
                .aniLabRankPreviews(aniLabRankDtos)
                .pageInfo(pageInfo)
                .build();
    }

    public void setupWeeklyVote(LocalDateTime lastWeekStartedAt, LocalDateTime now) {
        Week lastWeek = getWeekByTime(lastWeekStartedAt);
        int lastWeekQuarterValue = lastWeek.getQuarter().getQuarterValue();

        YQWRecord record = getThisWeekRecord(now);
        int thisQuarterValue = record.quarterValue();

        //=== 분기, 시즌 찾기(or 생성) & 주 생성 ===//
        boolean quarterCreateEnabled = lastWeekQuarterValue != thisQuarterValue;
        getOrCreateQuarterAndWeek(quarterCreateEnabled, record, getThisWeekStartedAt(now));

        //=== 애니 후보군 생성 ===//
//        List<Anime> nowShowingAnimes = animeService.getAnimesForCandidate(season, now);
//
//        List<AnimeCandidate> animeCandidates = nowShowingAnimes.stream()
//                .map(anime -> AnimeCandidate.create(savedWeek, anime))
//                .toList();
//        animeCandidateRepository.saveAll(animeCandidates);

        // TODO 캐릭터 후보군 생성

    }

    @Transactional
    public void getOrCreateQuarterAndWeek(
            boolean createEnabled,
            YQWRecord record,
            LocalDateTime weekStartedAt
    ) {
        int yearValue = record.yearValue();

        Quarter quarter = seasonService
                .getOrCreateQuarter(createEnabled, yearValue, record.quarterValue());
        seasonService.getOrCreateSeason(createEnabled, quarter);

        // getOrCreateWeek
        int weekValue = record.weekValue();
        Optional<Week> weekOpt = weekRepository
                .findByQuarter_IdAndWeekValue(quarter.getId(), weekValue);
        if (weekOpt.isEmpty()) {
            weekRepository.save(Week.create(
                    quarter,
                    weekValue,
                    weekStartedAt
            ));
        }
    }

    public List<WeekDto> getAllWeeks() {
        return weekRepository.findAll().stream()
                .filter(Week::getAnnouncePrepared)
                .sorted(Comparator.comparing(Week::getStartDateTime))
                .map(WeekDto::of)
                .toList();
    }
}
