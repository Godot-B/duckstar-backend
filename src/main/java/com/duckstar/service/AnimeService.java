package com.duckstar.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.AnimeHandler;
import com.duckstar.apiPayload.exception.handler.EpisodeHandler;
import com.duckstar.domain.Anime;
import com.duckstar.domain.Season;
import com.duckstar.domain.enums.AnimeStatus;
import com.duckstar.domain.mapping.AnimeCandidate;
import com.duckstar.domain.mapping.Episode;
import com.duckstar.repository.AnimeCharacter.AnimeCharacterRepository;
import com.duckstar.repository.AnimeOtt.AnimeOttRepository;
import com.duckstar.repository.AnimeSeason.AnimeSeasonRepository;
import com.duckstar.repository.AnimeRepository;
import com.duckstar.repository.AnimeCandidate.AnimeCandidateRepository;
import com.duckstar.repository.Episode.EpisodeRepository;
import com.duckstar.web.dto.AnimeResponseDto.AnimeHomeDto;
import com.duckstar.web.dto.RankInfoDto.DuckstarRankPreviewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.duckstar.web.dto.AnimeResponseDto.*;

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

    public Anime findByIdOrThrow(Long animeId) {
        return animeRepository.findById(animeId).orElseThrow(() ->
                new AnimeHandler(ErrorStatus.ANIME_NOT_FOUND));
    }

    public List<DuckstarRankPreviewDto> getAnimeRankPreviewsByWeekId(Long weekId, int size) {
        List<AnimeCandidate> animeCandidates =
                animeCandidateRepository.getAnimeCandidatesByWeekId(
                        weekId,
                        PageRequest.of(0, size)
                );

        return animeCandidates.stream()
                .map(DuckstarRankPreviewDto::from)
                .toList();
    }

    public AnimeHomeDto getAnimeHomeDtoById(Long animeId) {
        // 애니 정보, 분기 성적 통계
        Anime anime = findByIdOrThrow(animeId);

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
                .episodeDtos(
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
    public List<Anime> updateAndGetAnimes(boolean isQuarterChanged, Season season) {
        List<Anime> animes = animeRepository.findAllByStatusOrStatus(AnimeStatus.UPCOMING, AnimeStatus.NOW_SHOWING);
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime threshold = now.plusWeeks(1);

        return animes.stream()
                .peek(anime -> {
                    if (anime.getStatus() == AnimeStatus.UPCOMING) {
                        if (!threshold.isBefore(anime.getPremiereDateTime())) {
                            anime.setStatus(AnimeStatus.NOW_SHOWING);
                        }
                    } else if (anime.getStatus() == AnimeStatus.NOW_SHOWING) {
                        // 분기마다 수십 개 수준이라 가능,,
                        // 수백~ 수천 등 더 많아지면 N+1 이슈 고려해야 함.
                        LocalDateTime lastEpScheduledAt = findLastEpScheduledAt(anime).orElseThrow(() ->
                                        new EpisodeHandler(ErrorStatus.EPISODE_NOT_FOUND))
                                .getScheduledAt();

                        if (!now.isBefore(lastEpScheduledAt)) {
                            anime.setStatus(AnimeStatus.ENDED);
                        }
                    }
                })
                .filter(anime -> isQuarterChanged ?
                        anime.getStatus() == AnimeStatus.NOW_SHOWING :
                        anime.getStatus() == AnimeStatus.NOW_SHOWING && isEligibleForNextSeason(anime, season)
                )
                .toList();
    }

    private boolean isEligibleForNextSeason(Anime anime, Season season) {
        LocalDateTime cutOff = LocalDateTime.of(
                season.getYearValue(),
                season.getType().getStartDate().getMonth(),
                season.getType().getStartDate().getDayOfMonth(),
                22,
                0
        );
        return !anime.getPremiereDateTime().isBefore(cutOff);
    }

    private Optional<Episode> findLastEpScheduledAt(Anime anime) {
        return episodeRepository.findTopByAnimeOrderByEpisodeNumberDesc(anime);
    }
}
