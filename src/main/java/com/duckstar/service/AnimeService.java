package com.duckstar.service;

import com.duckstar.abroad.aniLab.Anilab;
import com.duckstar.abroad.aniLab.AnilabRepository;
import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.AnimeHandler;
import com.duckstar.apiPayload.exception.handler.WeekHandler;
import com.duckstar.abroad.animeTrend.AnimeTrending;
import com.duckstar.abroad.animeTrend.AnimeTrendingRepository;
import com.duckstar.domain.Anime;
import com.duckstar.domain.Season;
import com.duckstar.domain.Week;
import com.duckstar.domain.enums.AnimeStatus;
import com.duckstar.domain.mapping.AnimeCandidate;
import com.duckstar.domain.mapping.AnimeSeason;
import com.duckstar.domain.mapping.Episode;
import com.duckstar.domain.mapping.comment.AnimeComment;
import com.duckstar.repository.AnimeCharacter.AnimeCharacterRepository;
import com.duckstar.repository.AnimeComment.AnimeCommentRepository;
import com.duckstar.repository.AnimeOtt.AnimeOttRepository;
import com.duckstar.repository.AnimeSeason.AnimeSeasonRepository;
import com.duckstar.repository.AnimeRepository;
import com.duckstar.repository.AnimeCandidate.AnimeCandidateRepository;
import com.duckstar.repository.Episode.EpisodeRepository;
import com.duckstar.repository.Week.WeekRepository;
import com.duckstar.schedule.ScheduleState;
import com.duckstar.web.dto.AnimeResponseDto.AnimeHomeDto;
import com.duckstar.web.dto.RankInfoDto.DuckstarRankPreviewDto;
import com.duckstar.web.dto.admin.EpisodeRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static com.duckstar.web.dto.AnimeResponseDto.*;
import static com.duckstar.web.dto.EpisodeResponseDto.*;
import static com.duckstar.web.dto.RankInfoDto.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnimeService {

    private final AnimeRepository animeRepository;
    private final AnimeCandidateRepository animeCandidateRepository;
    private final AnimeSeasonRepository animeSeasonRepository;
    private final AnimeOttRepository animeOttRepository;
    private final AnimeCharacterRepository animeCharacterRepository;
    private final EpisodeRepository episodeRepository;
    private final AnimeCommentRepository animeCommentRepository;
    private final WeekRepository weekRepository;

    private final ScheduleState scheduleState;
    private final AnimeTrendingRepository animeTrendingRepository;
    private final AnilabRepository anilabRepository;

    public List<DuckstarRankPreviewDto> getAnimeRankPreviewsByWeekId(Long weekId, int size) {
        List<AnimeCandidate> animeCandidates =
                animeCandidateRepository.findCandidatesByWeekOrdered(
                        weekId,
                        PageRequest.of(0, size)
                );

        return animeCandidates.stream()
                .map(DuckstarRankPreviewDto::of)
                .toList();
    }

    public List<RankPreviewDto> getAnimeTrendingPreviewsByWeekId(Long weekId, int size) {
        List<AnimeTrending> animeTrendings =
                animeTrendingRepository.findAllByWeek_Id(
                        weekId,
                        PageRequest.of(0, size)
                );

        return animeTrendings.stream()
                .map(RankPreviewDto::of)
                .toList();
    }

    public List<RankPreviewDto> getAnilabPreviewsByWeekId(Long weekId, int size) {
        List<Anilab> anilabs =
                anilabRepository.findAllByWeek_Id(
                        weekId,
                        PageRequest.of(0, size)
                );

        return anilabs.stream()
                .map(RankPreviewDto::of)
                .toList();
    }

    public AnimeHomeDto getAnimeHomeDtoById(Long animeId) {
        // 애니 정보, 분기 성적 통계
        Anime anime = animeRepository.findById(animeId).orElseThrow(() ->
                new AnimeHandler(ErrorStatus.ANIME_NOT_FOUND));

        LocalDateTime premiereDateTime = anime.getPremiereDateTime();

        AnimeInfoDto animeInfoDto = AnimeInfoDto.builder()
                .medium(anime.getMedium())
                .status(anime.getStatus())
                .totalEpisodes(anime.getTotalEpisodes())
                .premiereDateTime(premiereDateTime)
                .titleKor(anime.getTitleKor())
                .titleOrigin(anime.getTitleOrigin())
                .dayOfWeek(anime.getDayOfWeek())
                .airTime(anime.getAirTime())
                .synopsis(anime.getSynopsis())
                .corp(anime.getCorp())
                .director(anime.getDirector())
                .genre(anime.getGenre())
                .author(anime.getAuthor())
                .minAge(anime.getMinAge())
                .officalSite(anime.getOfficialSite())
                .mainImageUrl(anime.getMainImageUrl())
                .mainThumbnailUrl(anime.getMainThumbnailUrl())
                .seasonDtos(
                        animeSeasonRepository.getSeasonDtosByAnimeId(animeId)
                )
                .ottDtos(
                        animeOttRepository.getOttDtosByAnimeId(animeId)
                )
                .build();

        AnimeStatDto animeStatDto = AnimeStatDto.builder()
                .debutRank(anime.getDebutRank())
                .debutDate(anime.getDebutDate())
                .peakRank(anime.getPeakRank())
                .peakDate(anime.getPeakDate())
                .weeksOnTop10(anime.getWeeksOnTop10())
                .build();

        return AnimeHomeDto.builder()
                .animeInfoDto(animeInfoDto)
                .animeStatDto(animeStatDto)
                .episodeResponseDtos(
                        episodeRepository.getEpisodeDtosByAnimeId(animeId)
                )
                .rackUnitDtos(
                        animeCandidateRepository.getRackUnitDtosByAnimeId(animeId)
                )
                .castPreviews(
                        animeCharacterRepository.getAllCharacterHomePreviewsByAnimeId(animeId)
                )
                .build();
    }

    @Transactional
    public List<Anime> getAnimesForCandidate(boolean isQuarterChanged, Season season, LocalDateTime now) {
        // 예정 제외한 시즌 애니메이션
        List<Anime> seasonAnimes = animeSeasonRepository.findAllBySeason_Id(season.getId()).stream()
                .map(AnimeSeason::getAnime)
                .filter(anime -> anime.getStatus() != AnimeStatus.UPCOMING)
                .toList();

        // 이번 주 첫 방영 애니
        List<Anime> thisWeekComingAnimes = animeRepository
                .findAllByPremiereDateTimeGreaterThanEqualAndPremiereDateTimeLessThan(now, now.plusWeeks(1));

        List<Anime> combinedList = new ArrayList<>();
        combinedList.addAll(seasonAnimes);
        combinedList.addAll(thisWeekComingAnimes);

        // 바뀐 주차까지만 NOW_SHOWING 합집합
        if (isQuarterChanged) {
            combinedList.addAll(animeRepository.findAllByStatus(AnimeStatus.NOW_SHOWING));
        }

        // 마지막에만 distinct 처리
        return combinedList.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    @Transactional
    public void updateAnimeStatusByMinute() {
        List<Anime> animes = animeRepository.findAllByStatusOrStatus(AnimeStatus.UPCOMING, AnimeStatus.NOW_SHOWING);

        LocalDateTime now = LocalDateTime.now();

        // 방영 상태 업데이트
        animes.forEach(anime -> updateAnimeStatus(now, anime));
    }

    public void updateAnimeStatus(LocalDateTime now, Anime anime) {
        LocalDateTime premiereDateTime = anime.getPremiereDateTime();
        if (premiereDateTime == null) {
            return;
        }

        if (anime.getStatus() == AnimeStatus.UPCOMING) {
            boolean isPremiered = !now.isBefore(premiereDateTime);
            if (isPremiered) {
                anime.setStatus(AnimeStatus.NOW_SHOWING);
                if (!animeCandidateRepository.existsByAnime_Id(anime.getId())) {
                    Week week = weekRepository.findWeekByStartDateTimeLessThanEqualAndEndDateTimeGreaterThan(now, now)
                            .orElseThrow(() -> new WeekHandler(ErrorStatus.WEEK_NOT_FOUND));

                    AnimeCandidate candidate = AnimeCandidate.create(week, anime);

                    animeCandidateRepository.save(candidate);
                }
            }
        } else if (anime.getStatus() == AnimeStatus.NOW_SHOWING) {
            // 분기마다 수십 개 수준이라 가능,,
            // 수백~ 수천 등 더 많아지면 N+1 이슈 고려해야 함.
            Episode episode = findLastEpScheduledAt(anime).orElse(null);
            LocalDateTime lastEpScheduledAt = episode != null ?
                    episode.getScheduledAt() :
                    null;

            boolean isAfterLastEpisode = lastEpScheduledAt != null && !now.isBefore(lastEpScheduledAt.plusMinutes(24));
            if (isAfterLastEpisode) {
                anime.setStatus(AnimeStatus.ENDED);
            }
        }
    }

    private Optional<Episode> findLastEpScheduledAt(Anime anime) {
        return episodeRepository.findTopByAnimeOrderByEpisodeNumberDesc(anime);
    }

    @Transactional
    public EpisodeResultDto updateTotalEpisodes(Long animeId, EpisodeRequestDto request) {
        if (scheduleState.isWeeklyScheduleRunning()) {
            throw new WeekHandler(ErrorStatus.SCHEDULE_RUNNING);
        }

        scheduleState.startAdminMode();
        try {
            Anime anime = animeRepository.findById(animeId)
                    .orElseThrow(() -> new AnimeHandler(ErrorStatus.ANIME_NOT_FOUND));

            Integer oldTotalEpisodes = anime.getTotalEpisodes();
            oldTotalEpisodes = oldTotalEpisodes == null ? 12 : oldTotalEpisodes;
            int lastEpisodeNumber = oldTotalEpisodes;

            Integer newTotalEpisodes = request.getTotalEpisodes();

            List<Episode> episodes = episodeRepository.findAllByAnime_IdOrderByEpisodeNumberAsc(animeId);

            if (oldTotalEpisodes.equals(newTotalEpisodes)) {
                anime.updateTotalEpisodes(12);
                return EpisodeResultDto.builder()
                        .message("에피소드 수 변화 없음")
                        .build();

            } else if (oldTotalEpisodes < newTotalEpisodes) {
                anime.updateTotalEpisodes(newTotalEpisodes);
                Episode lastEpisode = episodes.get(episodes.size() - 1);

                int diff = newTotalEpisodes - oldTotalEpisodes;
                LocalDateTime scheduledAt = lastEpisode.getNextEpScheduledAt();

                LocalDateTime nextEpScheduledAt;
                List<EpisodePreviewDto> addedEpisodes = new ArrayList<>();
                for (int i = 0; i < diff; i++) {
                    int episodeNumber = lastEpisodeNumber + i + 1;
                    nextEpScheduledAt = scheduledAt.plusWeeks(1);
                    Episode episode = Episode.create(
                            anime,
                            episodeNumber,
                            scheduledAt,
                            nextEpScheduledAt
                    );
                    Episode saved = episodeRepository.save(episode);

                    animeCommentRepository.findAllByAnime_IdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                            anime.getId(), scheduledAt, nextEpScheduledAt)
                    .forEach(ac -> ac.setEpisode(saved));

                    addedEpisodes.add(EpisodePreviewDto.of(episode));

                    scheduledAt = scheduledAt.plusWeeks(1);
                }
                return EpisodeResultDto.builder()
                        .message("에피소드 추가: " + diff + "개")
                        .addedEpisodes(addedEpisodes)
                        .build();

            } else {
                anime.updateTotalEpisodes(newTotalEpisodes);
                int newLastEpisodeNumber = newTotalEpisodes;

                // 삭제되어야 하는 에피소드 id들
                List<Long> episodeIdsToDelete = episodes.stream()
                        .filter(ep -> ep.getEpisodeNumber() > newLastEpisodeNumber &&
                                ep.getEpisodeNumber() <= lastEpisodeNumber)
                        .map(Episode::getId)
                        .toList();

                List<EpisodePreviewDto> deletedEpisodes = new ArrayList<>();
                int episodeNumber = newLastEpisodeNumber + 1;
                for (Long episodeId : episodeIdsToDelete) {
                    List<AnimeComment> comments = animeCommentRepository.findAllByEpisode_Id(episodeId);

                    // 댓글에서 끊어야 할 에피소드 관계 끊기
                    comments.forEach(ac -> ac.setEpisode(null));

                    deletedEpisodes.add(
                            EpisodePreviewDto.builder()
                                    .episodeId(episodeId)
                                    .episodeNumber(episodeNumber)
                                    .build()
                    );
                    episodeNumber += 1;
                }
                episodeRepository.deleteAllById(episodeIdsToDelete);

                int diff = oldTotalEpisodes - newTotalEpisodes;
                return EpisodeResultDto.builder()
                        .message("에피소드 삭제: " + diff + "개")
                        .deletedEpisodes(deletedEpisodes)
                        .build();
            }

        } finally {
            scheduleState.stopAdminMode();
        }
    }

    public Optional<Episode> findCurrentEpisode(Anime anime, LocalDateTime now) {
        return episodeRepository
                .findEpisodeByAnimeAndScheduledAtLessThanEqualAndNextEpScheduledAtGreaterThan(anime, now, now);
    }

    @Transactional
    public EpisodeResultDto setUnknown(Long animeId) {
        if (scheduleState.isWeeklyScheduleRunning()) {
            throw new WeekHandler(ErrorStatus.SCHEDULE_RUNNING);
        }

        scheduleState.startAdminMode();
        try {
            Anime anime = animeRepository.findById(animeId)
                    .orElseThrow(() -> new AnimeHandler(ErrorStatus.ANIME_NOT_FOUND));

            anime.updateTotalEpisodes(null);

            return EpisodeResultDto.builder()
                    .message("에피소드 수 미정")
                    .build();

        } finally {
            scheduleState.stopAdminMode();
        }
    }
}
