package com.duckstar.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.WeekHandler;
import com.duckstar.domain.HomeBanner;
import com.duckstar.domain.Week;
import com.duckstar.repository.HomeBannerRepository;
import com.duckstar.repository.Week.WeekRepository;
import com.duckstar.web.dto.HomeDto;
import com.duckstar.web.dto.HomeDto.HomeBannerDto;
import com.duckstar.web.dto.HomeDto.WeeklyTopDto;
import com.duckstar.web.dto.RankInfoDto.DuckstarRankPreviewDto;
import com.duckstar.web.dto.WeekResponseDto.WeekDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {
    private final AnimeService animeService;

    private final WeekRepository weekRepository;
    private final HomeBannerRepository homeBannerRepository;

    public HomeDto getHome(int size) {
        LocalDateTime now = LocalDateTime.now();
        List<Week> past12Weeks = weekRepository
                .findByStartDateTimeLessThanEqualOrderByStartDateTimeDesc(
                        now,
                        PageRequest.of(0, 12)
                );

        Week currentWeek = past12Weeks.stream()
                .findFirst()
                .orElseThrow(() -> new WeekHandler(ErrorStatus.WEEK_NOT_FOUND));

        List<DuckstarRankPreviewDto> animeDuckstarRankPreviews =
                animeService.getAnimeRankPreviewsByWeekId(currentWeek.getId(), size);

        List<HomeBanner> homeBanners =
                homeBannerRepository.getHomeBannersByWeekId(currentWeek.getId());

        List<HomeBannerDto> homeBannerDtos = homeBanners.stream()
                .map(HomeBannerDto::from)
                .toList();

        WeeklyTopDto weeklyTopDto = WeeklyTopDto.builder()
                .duckstarRankPreviews(animeDuckstarRankPreviews)
                .crawlerRankDtos(null)
                .build();

        List<WeekDto> weekDtos = past12Weeks.stream()
                .map(WeekDto::from)
                .toList();

        return HomeDto.builder()
                .weeklyTopDto(weeklyTopDto)
                .homeBannerDtos(homeBannerDtos)
                .weekDtos(weekDtos)
                .build();
    }

    public WeeklyTopDto getAnimeWeeklyTopDto(Long weekId, int size) {
        return WeeklyTopDto.builder()
                .duckstarRankPreviews(
                        animeService.getAnimeRankPreviewsByWeekId(weekId, size)
                )
                .crawlerRankDtos(
                        null
                )
                .build();
    }
}
