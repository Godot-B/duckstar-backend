package com.duckstar.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.AnimeHandler;
import com.duckstar.domain.Anime;
import com.duckstar.repository.AnimeCharacter.AnimeCharacterRepository;
import com.duckstar.repository.AnimeOtt.AnimeOttRepository;
import com.duckstar.repository.AnimeSeason.AnimeSeasonRepository;
import com.duckstar.repository.AnimeRepository;
import com.duckstar.repository.AnimeWeek.WeekAnimeRepository;
import com.duckstar.web.dto.AnimeResponseDto.AnimeHomeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.duckstar.web.dto.AnimeResponseDto.*;

@Service
@RequiredArgsConstructor
public class AnimeService {

    private final AnimeRepository animeRepository;
    private final WeekAnimeRepository weekAnimeRepository;
    private final AnimeSeasonRepository animeSeasonRepository;
    private final AnimeOttRepository animeOttRepository;
    private final AnimeCharacterRepository animeCharacterRepository;

    public AnimeHomeDto getAnimeHomeDtoById(Long animeId) {
        // 애니 정보, 분기 성적 통계
        Anime anime = animeRepository.findById(animeId).orElseThrow(() ->
                new AnimeHandler(ErrorStatus.ANIME_NOT_FOUND));

        LocalDateTime premiereDateTime = anime.getPremiereDateTime();

        AnimeInfoDto info = AnimeInfoDto.builder()
                .medium(anime.getMedium())
                .status(anime.getStatus())
                .totalEpisodes(anime.getTotalEpisodes())
                .premiereDateTime(premiereDateTime)
                .titleKor(anime.getTitleKor())
                .titleOrigin(anime.getTitleOrigin())
                .dayOfWeek(anime.getDayOfWeek())
                .airTime(anime.getAirTime())
                .corp(anime.getCorp())
                .director(anime.getDirector())
                .genre(anime.getGenre())
                .author(anime.getAuthor())
                .minAge(anime.getMinAge())
                .officalSite(anime.getOfficialSite())
                .mainImageUrl(anime.getMainImageUrl())
                .seasonDtos(
                        animeSeasonRepository.getSeasonDtosByAnimeId(animeId)
                )
                .ottDtos(
                        animeOttRepository.getOttDtosByAnimeId(animeId)
                )
                .build();

        AnimeStatDto stat = AnimeStatDto.builder()
                .debutRank(anime.getDebutRank())
                .debutDate(anime.getDebutDate())
                .peakRank(anime.getPeakRank())
                .peakDate(anime.getPeakDate())
                .weeksOnTop10(anime.getWeeksOnTop10())
                .build();

        // 댓글


        return AnimeHomeDto.builder()
                .info(info)
                .stat(stat)
                .weekDataDto(
                        weekAnimeRepository.getWeekDataByAnimeInfo(animeId, premiereDateTime)
                )
                .characterHomePreviewDtos(
                        animeCharacterRepository.getAllCharacterHomePreviewsByAnimeId(animeId)
                )
                .build();
    }
}
