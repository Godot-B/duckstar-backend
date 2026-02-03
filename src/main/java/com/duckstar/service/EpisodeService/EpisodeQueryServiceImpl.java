package com.duckstar.service.EpisodeService;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.AnimeHandler;
import com.duckstar.apiPayload.exception.handler.EpisodeHandler;
import com.duckstar.apiPayload.exception.handler.WeekHandler;
import com.duckstar.domain.Week;
import com.duckstar.repository.AnimeRepository;
import com.duckstar.repository.Episode.EpisodeRepository;
import com.duckstar.repository.Week.WeekRepository;
import com.duckstar.schedule.ScheduleHandler;
import com.duckstar.service.WeekService;
import com.duckstar.web.dto.admin.ContentResponseDto.AdminEpisodeListDto;
import com.duckstar.web.support.VoteCookieManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.duckstar.web.dto.VoteResponseDto.*;
import static com.duckstar.web.dto.WeekResponseDto.*;
import static com.duckstar.web.dto.admin.ContentResponseDto.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EpisodeQueryServiceImpl implements EpisodeQueryService {
    private final EpisodeRepository episodeRepository;
    private final WeekRepository weekRepository;
    private final AnimeRepository animeRepository;

    private final WeekService weekService;

    private final VoteCookieManager voteCookieManager;
    private final ScheduleHandler scheduleHandler;

    /**
     * 별점 투표 방식
     */
    public LiveCandidateListDto getLiveCandidatesByWindow(
            Long memberId,
            HttpServletRequest requestRaw
    ) {
        // 사용자의 principal_key 최대 2개 (2주 걸치는 시간 존재)
        List<String> cookies = voteCookieManager.readAllCookies(requestRaw);
        List<String> principalKeys = cookies.isEmpty() ?
                Stream.of(voteCookieManager.toPrincipalKey(memberId, null))
                        .filter(Objects::nonNull)
                        .toList() :
                cookies.stream()
                        .map(c -> voteCookieManager.toPrincipalKey(memberId, c))
                        .filter(Objects::nonNull)
                        .toList();

        // 전체 VOTING_WINDOW 상태 에피소드들 조회
        List<LiveCandidateDto> candidates = episodeRepository
                .getLiveCandidateDtos(principalKeys);

        LocalDateTime now = LocalDateTime.now();
        Week currentWeek = scheduleHandler.getSafeWeekByTime(now);
        Integer currentWeekValue = currentWeek.getWeekValue();

        // 분리: 이번 주
        List<LiveCandidateDto> currentWeekStarCandidates =
                candidates.stream()
                        .filter(c -> c.getWeek().equals(currentWeekValue))
                        .toList();

        // 분리: 지난 주
        List<LiveCandidateDto> lastWeekStarCandidates =
                candidates.stream()
                        .filter(c -> !c.getWeek().equals(currentWeekValue))
                        .toList();

        return LiveCandidateListDto.builder()
                .weekDto(WeekDto.of(currentWeek))
                .currentWeekLiveCandidates(currentWeekStarCandidates)
                .lastWeekLiveCandidates(lastWeekStarCandidates)
                .build();
    }

    public List<WeekCandidateDto> getWeekCandidatesByYQW(
            Integer year,
            Integer quarter,
            Integer week,
            Long memberId,
            HttpServletRequest requestRaw
    ) {
        String cookieId = voteCookieManager.readCookie(
                requestRaw,
                year,
                quarter,
                week
        );
        String principalKey = voteCookieManager.toPrincipalKey(memberId, cookieId);

        Long weekId = weekService.getWeekIdByYQW(year, quarter, week);

        return episodeRepository.getWeekCandidateDtos(weekId, principalKey);
    }

    public CandidateFormDto getCandidateForm(
            Long episodeId,
            Long memberId,
            HttpServletRequest requestRaw
    ) {
        // 사용자의 principal_key 최대 2개 (2주 걸치는 시간 존재)
        List<String> cookies = voteCookieManager.readAllCookies(requestRaw);
        List<String> principalKeys = cookies.isEmpty() ?
                Stream.of(voteCookieManager.toPrincipalKey(memberId, null))
                        .filter(Objects::nonNull)
                        .toList() :
                cookies.stream()
                        .map(c -> voteCookieManager.toPrincipalKey(memberId, c))
                        .filter(Objects::nonNull)
                        .toList();

        return episodeRepository.getCandidateFormDto(episodeId, principalKeys)
                .orElseThrow(() -> new EpisodeHandler(ErrorStatus.EPISODE_NOT_FOUND));
    }

    @Override
    public AdminEpisodeListDto getAdminEpisodesByAnimeId(Long animeId) {
        animeRepository.findById(animeId).orElseThrow(() ->
                new AnimeHandler(ErrorStatus.ANIME_NOT_FOUND));

        List<AdminEpisodeDto> adminEpisodeDtos =
                episodeRepository.getEpisodeInfoDtosByAnimeId(animeId);

        return AdminEpisodeListDto.builder()
                .episodeTotalCount(adminEpisodeDtos.size())
                .adminEpisodeDtos(adminEpisodeDtos)
                .build();
    }

    @Override
    public AdminScheduleInfoDto getAdminScheduleByWeekId(Long weekId) {
        Week week = weekRepository.findWeekById(weekId).orElseThrow(() ->
                new WeekHandler(ErrorStatus.WEEK_NOT_FOUND));

        List<ScheduleInfoDto> scheduleInfoDtos =
                episodeRepository.getScheduleInfoDtosByWeekId(week);

        return AdminScheduleInfoDto.builder()
                .weekDto(WeekDto.of(week))
                .animeTotalCount(scheduleInfoDtos.size())
                .scheduleInfoDtos(scheduleInfoDtos)
                .build();
    }
}
