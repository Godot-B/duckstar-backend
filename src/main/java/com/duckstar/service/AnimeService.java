package com.duckstar.service;

import com.duckstar.abroad.aniLab.Anilab;
import com.duckstar.abroad.aniLab.AnilabRepository;
import com.duckstar.abroad.reader.CsvImportService;
import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.AnimeHandler;
import com.duckstar.apiPayload.exception.handler.WeekHandler;
import com.duckstar.abroad.animeCorner.AnimeCorner;
import com.duckstar.abroad.animeCorner.AnimeCornerRepository;
import com.duckstar.domain.*;
import com.duckstar.domain.enums.*;
import com.duckstar.domain.mapping.AnimeCandidate;
import com.duckstar.domain.mapping.AnimeOtt;
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
import com.duckstar.repository.OttRepository;
import com.duckstar.repository.Week.WeekRepository;
import com.duckstar.s3.S3Uploader;
import com.duckstar.schedule.ScheduleState;
import com.duckstar.web.dto.AnimeResponseDto.AnimeHomeDto;
import com.duckstar.web.dto.OttDto;
import com.duckstar.web.dto.RankInfoDto.DuckstarRankPreviewDto;
import com.duckstar.web.dto.admin.EpisodeRequestDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.util.*;
import java.util.stream.Collectors;

import static com.duckstar.web.dto.AnimeResponseDto.*;
import static com.duckstar.web.dto.EpisodeResponseDto.*;
import static com.duckstar.web.dto.RankInfoDto.*;
import static com.duckstar.web.dto.admin.AnimeRequestDto.*;

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
    private final AnimeCornerRepository animeCornerRepository;
    private final AnilabRepository anilabRepository;

    private final CsvImportService csvImportService;
    private final SeasonService seasonService;
    private final OttRepository ottRepository;
    private final S3Uploader s3Uploader;

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
    public List<Anime> getAnimesForCandidate(Season season, LocalDateTime now) {
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

        // 마지막에만 distinct 처리
        Set<Long> ids = new HashSet<>();
        return combinedList.stream()
                .filter(anime -> ids.add(anime.getId()))
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

            // 24분 하드코딩. 나중 생각
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
    public Long addAnime(PostRequestDto request) throws IOException {
        LocalDateTime premiereDateTime = request.getPremiereDateTime();

        Integer totalEpisodes = request.getTotalEpisodes();
        boolean isTVA = request.getMedium() == Medium.TVA;
        if (isTVA && totalEpisodes == null) {
            totalEpisodes = 12;
        }

        //=== 방영 여부 결정 ===//
        AnimeStatus status = AnimeStatus.UPCOMING;
        if (premiereDateTime != null) {
            LocalDateTime now = LocalDateTime.now();

            if (!now.isBefore(premiereDateTime)) {
                status = AnimeStatus.NOW_SHOWING;
            }

            if (isTVA) {
                LocalDateTime lastEpScheduledAt = premiereDateTime.plusWeeks(totalEpisodes - 1);
                if (!now.isBefore(lastEpScheduledAt.plusMinutes(24))) status = AnimeStatus.ENDED;
            }
        }

        Map<SiteType, String> officialSite = Map.of();
        // officialSite String 파싱
        if (request.getOfficialSiteString() != null) {
            ObjectMapper mapper = new ObjectMapper();
            officialSite = mapper.readValue(
                    request.getOfficialSiteString(),
                    new TypeReference<>() {
                    }
            );
        }

        //=== 애니메이션 저장 ===//
        Anime anime = Anime.builder()
                .titleKor(request.getTitleKor())
                .titleOrigin(request.getTitleOrigin())
                .titleEng(request.getTitleEng())

                .medium(request.getMedium())

                .status(status)
                .airTime(request.getAirTime())
                .premiereDateTime(premiereDateTime)
                .dayOfWeek(request.getDayOfWeek())
                .totalEpisodes(totalEpisodes)

                .corp(request.getCorp())
                .director(request.getDirector())
                .genre(request.getGenre())
                .author(request.getAuthor())
                .minAge(request.getMinAge())
                .officialSite(officialSite)
                .synopsis(request.getSynopsis())

                // main, thumb 은 아래에서 업데이트
                .build();

        Anime saved = animeRepository.save(anime);

        //=== webp 변환, s3 업로드, DB UPDATE ===//
        csvImportService.uploadAnimeMain(request.getMainImage(), saved);

        if (premiereDateTime != null) {
            //=== 시즌 연관관계 생성 ===//
            MonthDay prDate = MonthDay.from(premiereDateTime);
            SeasonType prSeason = Arrays.stream(SeasonType.values())
                    .sorted(Comparator.comparing(SeasonType::getStartDate)) // 3/21, 6/21, 9/23, 12/22
                    .filter(seasonType -> !prDate.isBefore(seasonType.getStartDate()))
                    .reduce((first, second) -> second) // 마지막으로 매칭된 시즌
                    .orElse(SeasonType.WINTER);

            Quarter quarter = seasonService
                    .getOrCreateQuarter(true, premiereDateTime.getYear(), prSeason.getQuarter());
            Season season = seasonService.getOrCreateSeason(true, quarter);

            animeSeasonRepository.save(AnimeSeason.create(anime, season));

            if (isTVA) {
                //=== 에피소드 생성 ===//
                List<Episode> episodes = new ArrayList<>();
                LocalDateTime scheduledAt = premiereDateTime;
                for (int i = 0; i < totalEpisodes; i++) {
                    LocalDateTime nextEpScheduledAt = scheduledAt.plusWeeks(1);
                    episodes.add(Episode.create(
                            saved,
                            i + 1,
                            scheduledAt,
                            nextEpScheduledAt
                    ));
                    scheduledAt = nextEpScheduledAt;
                }
                episodeRepository.saveAll(episodes);
            }
        }

        //=== OTT 연관관계 생성 ===//
        List<OttDto> ottDtos = request.getOttDtos();
        if (ottDtos != null && !ottDtos.isEmpty()) {
            Map<OttType, Ott> ottMap = ottRepository.findAll()
                    .stream()
                    .collect(Collectors.toMap(
                            Ott::getType,
                            ott -> ott
                            )
                    );
            ottDtos.forEach(ottDto ->
                    animeOttRepository.save(
                            AnimeOtt.create(
                                    anime,
                                    ottMap.get(ottDto.getOttType()),
                                    ottDto.getWatchUrl()
                            )
                    ));
        }



        return saved.getId();
    }

    @Transactional
    public Long updateAnimeImage(Long animeId, ImageRequestDto request) throws IOException {
        Anime anime = animeRepository.findById(animeId).orElseThrow(() ->
                new AnimeHandler(ErrorStatus.ANIME_NOT_FOUND));

        String mainUrl = anime.getMainImageUrl();
        int oldMainVer = extractVersion("main", mainUrl);
        String thumbUrl = anime.getMainThumbnailUrl();
        int oldThumbVer = extractVersion("thumb", thumbUrl);

        MultipartFile reqMain = request.getMainImage();
        if (reqMain != null) {
            csvImportService.updateAnimeMain(reqMain, Math.max(oldMainVer, oldThumbVer) + 1, anime);

            s3Uploader.delete(mainUrl);
            s3Uploader.delete(thumbUrl);

            return anime.getId();
        } else {
            return null;
        }
    }

    private int extractVersion(String type, String imageUrl) {
        // ".webp" 제거
        String noExt = imageUrl.replaceFirst("\\.webp$", "");
        // "main" 뒷부분 추출
        String versionString = noExt.substring(noExt.lastIndexOf(type) + type.length());

        if (versionString.startsWith("_v")) {
            return Integer.parseInt(versionString.substring(2));
        }
        return 2; // 기본값
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
