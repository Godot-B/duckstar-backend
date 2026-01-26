package com.duckstar.service.AnimeService;

import com.duckstar.abroad.aniLab.Anilab;
import com.duckstar.abroad.aniLab.AnilabRepository;
import com.duckstar.abroad.animeCorner.AnimeCorner;
import com.duckstar.abroad.animeCorner.AnimeCornerRepository;
import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.AnimeHandler;
import com.duckstar.domain.Anime;
import com.duckstar.domain.enums.DayOfWeekShort;
import com.duckstar.domain.mapping.weeklyVote.Episode;
import com.duckstar.repository.AnimeCharacter.AnimeCharacterRepository;
import com.duckstar.repository.AnimeOtt.AnimeOttRepository;
import com.duckstar.repository.AnimeRepository;
import com.duckstar.repository.AnimeQuarter.AnimeQuarterRepository;
import com.duckstar.repository.Episode.EpisodeRepository;
import com.duckstar.web.dto.RankInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static com.duckstar.web.dto.AnimeResponseDto.*;
import static com.duckstar.web.dto.RankInfoDto.*;
import static com.duckstar.web.dto.admin.ContentResponseDto.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnimeQueryServiceImpl implements AnimeQueryService {
    private final AnimeRepository animeRepository;
    private final AnimeQuarterRepository animeQuarterRepository;
    private final AnimeOttRepository animeOttRepository;
    private final AnimeCharacterRepository animeCharacterRepository;
    private final EpisodeRepository episodeRepository;
    private final AnimeCornerRepository animeCornerRepository;
    private final AnilabRepository anilabRepository;

    @Override
    public List<DuckstarRankPreviewDto> getAnimeRankPreviewsByWeekId(Long weekId, int size) {
        List<Episode> episodes =
                episodeRepository.findEpisodesByWeekOrdered(
                        weekId,
                        PageRequest.of(0, size)
                );

        return episodes.stream()
                .map(DuckstarRankPreviewDto::of)
                .toList();
    }

    @Override
    public List<RankPreviewDto> getAnimeCornerPreviewsByWeekId(Long weekId, int size) {
        List<AnimeCorner> animeCorners =
                animeCornerRepository.findAllByWeek_Id(
                        weekId,
                        PageRequest.of(0, size)
                );

        return animeCorners.stream()
                .map(RankPreviewDto::of)
                .toList();
    }

    @Override
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

    @Override
    public List<Long> getAllAnimeIds() {
        return animeRepository.findAll().stream()
                .map(Anime::getId)
                .toList();
    }

    @Override
    public AnimeHomeDto getAnimeHomeDtoById(Long animeId) {
        // 애니 정보, 분기 성적 통계
        Anime anime = animeRepository.findById(animeId).orElseThrow(() ->
                new AnimeHandler(ErrorStatus.ANIME_NOT_FOUND));

        LocalDateTime premiereDateTime = anime.getPremiereDateTime();

        LocalTime airTime = anime.getAirTime();
        AnimeInfoDto animeInfoDto = AnimeInfoDto.builder()
                .medium(anime.getMedium())
                .status(anime.getStatus())
                .totalEpisodes(anime.getTotalEpisodes())
                .premiereDateTime(premiereDateTime)
                .titleKor(anime.getTitleKor())
                .titleOrigin(anime.getTitleOrigin())
                .dayOfWeek(
                        DayOfWeekShort.getLogicalDay(airTime ,anime.getDayOfWeek())
                )
                .airTime(airTime)
                .synopsis(anime.getSynopsis())
                .corp(anime.getCorp())
                .director(anime.getDirector())
                .genre(anime.getGenre())
                .author(anime.getAuthor())
                .minAge(anime.getMinAge())
                .officialSite(anime.getOfficialSite())
                .mainImageUrl(anime.getMainImageUrl())
                .mainThumbnailUrl(anime.getMainThumbnailUrl())
                .quarterDtos(
                        animeQuarterRepository.getQuarterDtosByAnimeId(animeId)
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
                        null
                        /*animeCandidateRepository.getRackUnitDtosByAnimeId(animeId)*/
                )
                .castPreviews(
                        animeCharacterRepository.getAllCharacterHomePreviewsByAnimeId(animeId)
                )
                .build();
    }

    public Optional<Episode> findCurrentEpisode(Anime anime, LocalDateTime now) {
        return episodeRepository
                .findEpisodeByAnimeAndScheduledAtLessThanEqualAndNextEpScheduledAtGreaterThan(anime, now, now);
    }

    @Override
    public AdminAnimeListDto getAdminAnimeListDto(Long quarterId, Pageable pageable) {

        Page<AdminAnimeDto> items = animeQuarterRepository
                .getAdminAnimeDtosByQuarterId(quarterId, pageable);

        return AdminAnimeListDto.builder()
                .adminAnimeDtos(items.getContent())
                .page(items.getNumber())
                .size(items.getSize())
                .totalPages(items.getTotalPages())
                .totalElements(items.getTotalElements())
                .isFirst(items.isFirst())
                .isLast(items.isLast())
                .build();
    }
}
