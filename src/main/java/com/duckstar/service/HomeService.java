package com.duckstar.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.WeekHandler;
import com.duckstar.domain.HomeBanner;
import com.duckstar.domain.Week;
import com.duckstar.domain.enums.VoteStatus;
import com.duckstar.repository.AnimeCandidate.AnimeCandidateRepository;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static com.duckstar.web.dto.RankInfoDto.*;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {
    private final WeekRepository weekRepository;
    private final HomeBannerRepository homeBannerRepository;

    private final AnimeService animeService;
    private final AnimeCandidateRepository animeCandidateRepository;
    private final WeekService weekService;

    public HomeDto getHome(int size) {
        LocalDateTime now = LocalDateTime.now();
        List<Week> nowToPast12Weeks = weekRepository
                .findByStartDateTimeLessThanEqualOrderByStartDateTimeDesc(
                        now,
                        PageRequest.of(0, 12)
                ).stream()
                .toList();

        List<Week> pastWeeks = nowToPast12Weeks.stream()
                .filter(week -> week.getStatus() == VoteStatus.CLOSED)
                .toList();
        Week lastWeek = pastWeeks.stream()
                .findFirst()
                .orElseThrow(() -> new WeekHandler(ErrorStatus.WEEK_NOT_FOUND));

        List<HomeBanner> homeBanners =
                homeBannerRepository.getHomeBannersByWeekIdOrderByBannerNumberAsc(lastWeek.getId());

        if (homeBanners.isEmpty()) {
            Week secondLastWeek = pastWeeks.get(1);
            if (secondLastWeek != null) {
                homeBanners =
                        homeBannerRepository.getHomeBannersByWeekIdOrderByBannerNumberAsc(secondLastWeek.getId());
            }
        }

        List<HomeBannerDto> homeBannerDtos = homeBanners.stream()
                .map(HomeBannerDto::of)
                .toList();

        List<WeekDto> weekDtos = nowToPast12Weeks.stream()
                .map(WeekDto::of)
                .toList();

        return HomeDto.builder()
                .weeklyTopDto(
                        getAnimeWeeklyTop(lastWeek.getId(), size)
                )
                .homeBannerDtos(homeBannerDtos)
                .weekDtos(weekDtos)
                .build();
    }

    public WeeklyTopDto getAnimeWeeklyTop(Long weekId, int size) {
        List<DuckstarRankPreviewDto> duckstarRankPreviews = animeService.getAnimeRankPreviewsByWeekId(weekId, size);
        boolean isPrepared;
        if (!duckstarRankPreviews.isEmpty()) {
            Integer rank = duckstarRankPreviews.stream()
                    .findFirst().get()
                    .getRankPreviewDto()
                    .getRank();

            isPrepared = rank != null && rank > 0;  // 첫 번째 아이템의 순위가 존재할 때만 true
        } else {
            isPrepared = false;
        }

        // 준비되지 않았다면 현재 주차 후보들 랜덤 순서로 전송
        if (!isPrepared) {
            duckstarRankPreviews = animeCandidateRepository.findAllRandomByWeekId(
                    weekService.getCurrentWeek().getId(),
                            PageRequest.of(0, size)
                    )
                    .stream()
                    .map(DuckstarRankPreviewDto::of)
                    .toList();
        }

        return WeeklyTopDto.builder()
                .isPrepared(isPrepared)
                .duckstarRankPreviews(
                        duckstarRankPreviews
                )
                .anilabRankPreviews(
                        animeService.getAnilabPreviewsByWeekId(weekId, size)
                )
                .animeTrendingRankPreviews(
                        animeService.getAnimeTrendingPreviewsByWeekId(weekId, size)
                )
                .build();
    }
}
