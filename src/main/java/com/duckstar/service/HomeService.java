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
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {
    private final WeekRepository weekRepository;
    private final HomeBannerRepository homeBannerRepository;

    private final AnimeService animeService;

    public HomeDto getHome(int size) {
        LocalDateTime now = LocalDateTime.now();
        List<Week> nowToPast12Weeks = weekRepository
                .findByStartDateTimeLessThanEqualOrderByStartDateTimeDesc(
                        now,
                        PageRequest.of(0, 12)
                ).stream()
                .toList();

        Week currentWeek = nowToPast12Weeks.get(0);

        List<Week> pastWeeks = nowToPast12Weeks.stream()
                .filter(week -> week.getStatus() == VoteStatus.CLOSED && week.getAnnouncePrepared())
                .toList();
        Week lastWeek = pastWeeks.stream()
                .findFirst()
                .orElseThrow(() -> new WeekHandler(ErrorStatus.WEEK_NOT_FOUND));

        List<HomeBanner> homeBanners =
                homeBannerRepository.getHomeBannersByWeekIdOrderByBannerNumberAsc(lastWeek.getId());

        if (homeBanners.isEmpty() && pastWeeks.size() >= 2) {
            Week secondLastWeek = pastWeeks.get(1);
            if (secondLastWeek != null) {
                homeBanners =
                        homeBannerRepository.getHomeBannersByWeekIdOrderByBannerNumberAsc(secondLastWeek.getId());
            }
        }

        List<HomeBannerDto> homeBannerDtos = homeBanners.stream()
                .map(HomeBannerDto::ofAnime)
                .toList();

        List<WeekDto> weekDtos = pastWeeks.stream()
                .map(WeekDto::of)
                .toList();

        return HomeDto.builder()
                .weeklyTopDto(
                        getAnimeWeeklyTop(lastWeek.getId(), size)
                )
                .homeBannerDtos(homeBannerDtos)
                .currentWeekDto(WeekDto.of(currentWeek))
                .pastWeekDtos(weekDtos)
                .build();
    }

    public WeeklyTopDto getAnimeWeeklyTop(Long weekId, int size) {
        Week week = weekRepository.findById(weekId)
                .orElseThrow(() -> new WeekHandler(ErrorStatus.WEEK_NOT_FOUND));

        if (!week.getAnnouncePrepared()) throw new WeekHandler(ErrorStatus.ANNOUNCEMENT_NOT_PREPARED);

        List<DuckstarRankPreviewDto> duckstarRankPreviews =
                animeService.getAnimeRankPreviewsByWeekId(
                        weekId,
                        size
                );

        return WeeklyTopDto.builder()
                .duckstarRankPreviews(
                        duckstarRankPreviews
                )
                .anilabRankPreviews(
                        animeService.getAnilabPreviewsByWeekId(weekId, size)
                )
                .animeCornerRankPreviews(
                        animeService.getAnimeCornerPreviewsByWeekId(weekId, size)
                )
                .build();
    }
}
