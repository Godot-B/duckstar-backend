package com.duckstar.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.AnimeHandler;
import com.duckstar.domain.Anime;
import com.duckstar.domain.Week;
import com.duckstar.domain.enums.DayOfWeekShort;
import com.duckstar.domain.enums.SiteType;
import com.duckstar.domain.mapping.AnimeCandidate;
import com.duckstar.repository.AnimeCharacter.AnimeCharacterRepository;
import com.duckstar.repository.AnimeOtt.AnimeOttRepository;
import com.duckstar.repository.AnimeSeason.AnimeSeasonRepository;
import com.duckstar.repository.AnimeRepository;
import com.duckstar.repository.AnimeCandidate.AnimeCandidateRepository;
import com.duckstar.repository.Episode.EpisodeRepository;
import com.duckstar.web.dto.AnimeResponseDto.AnimeHomeDto;
import com.duckstar.web.dto.SearchResponseDto.AnimePreviewDto;
import com.duckstar.web.dto.RankInfoDto.DuckstarRankPreviewDto;
import com.duckstar.web.dto.SearchResponseDto.AnimePreviewListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.duckstar.web.dto.AnimeResponseDto.*;
import static com.duckstar.web.dto.WeekResponseDto.*;

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
    private final WeekService weekService;

    public Anime findByIdOrThrow(Long animeId) {
        return animeRepository.findById(animeId).orElseThrow(() ->
                new AnimeHandler(ErrorStatus.ANIME_NOT_FOUND));
    }

    public AnimePreviewListDto getScheduleByQuarterId(Long quarterId) {
        Week currentWeek = weekService.getCurrentWeek();
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
                .weekDto(WeekDto.from(currentWeek))
                .schedule(schedule)
                .build();
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
}
