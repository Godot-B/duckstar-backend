package com.duckstar.service.AnimeService;


import com.duckstar.abroad.reader.CsvImportService;
import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.AnimeHandler;
import com.duckstar.apiPayload.exception.handler.WeekHandler;
import com.duckstar.domain.Anime;
import com.duckstar.domain.Ott;
import com.duckstar.domain.Quarter;
import com.duckstar.domain.Season;
import com.duckstar.domain.enums.*;
import com.duckstar.domain.mapping.AnimeOtt;
import com.duckstar.domain.mapping.AnimeSeason;
import com.duckstar.domain.mapping.weeklyVote.Episode;
import com.duckstar.domain.mapping.comment.AnimeComment;
import com.duckstar.repository.AnimeComment.AnimeCommentRepository;
import com.duckstar.repository.AnimeOtt.AnimeOttRepository;
import com.duckstar.repository.AnimeRepository;
import com.duckstar.repository.AnimeSeason.AnimeSeasonRepository;
import com.duckstar.repository.Episode.EpisodeRepository;
import com.duckstar.repository.OttRepository;
import com.duckstar.repository.Week.WeekRepository;
import com.duckstar.s3.S3Uploader;
import com.duckstar.schedule.ScheduleState;
import com.duckstar.service.SeasonService;
import com.duckstar.web.dto.EpisodeResponseDto;
import com.duckstar.web.dto.OttDto;
import com.duckstar.web.dto.admin.AnimeRequestDto;
import com.duckstar.web.dto.admin.EpisodeRequestDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.util.*;
import java.util.stream.Collectors;

import static com.duckstar.web.dto.admin.AnimeRequestDto.*;

@Service
@RequiredArgsConstructor
@Transactional
public class AnimeCommandServiceImpl implements AnimeCommandService {

    private final AnimeCommentRepository animeCommentRepository;
    private final WeekRepository weekRepository;

    private final ScheduleState scheduleState;

    private final CsvImportService csvImportService;
    private final SeasonService seasonService;
    private final OttRepository ottRepository;
    private final S3Uploader s3Uploader;
    private final AnimeSeasonRepository animeSeasonRepository;
    private final AnimeRepository animeRepository;
    private final EpisodeRepository episodeRepository;
    private final AnimeOttRepository animeOttRepository;

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

    public record PremieredEpRecord(
            Episode episode,
            Boolean isFirstEpisode,
            Boolean isLastEpisode,

            // 정책
            Boolean isAfter24Minutes,
            Boolean isAfter36Hours,

            Anime anime
    ) {}

    @Transactional
    public void updateStatesByWindows() {
        LocalDateTime now = LocalDateTime.now();

        // 1분 윈도우, 24분 전 윈도우, 36시간 전 윈도우
        // 에피소드들 조회
        List<PremieredEpRecord> records = episodeRepository
                .findPremieredEpRecordsInWindow(now.minusSeconds(30), now.plusSeconds(30));

        records.forEach(record -> {
            Boolean premiereFinished = record.isAfter24Minutes;
            Boolean liveVoteFinished = record.isAfter36Hours;

            // 36시간 전 윈도우 내
            if (liveVoteFinished) {
                Episode episode = record.episode;
                episode.setEvaluateState(EpEvaluateState.LOGIN_REQUIRED);

                // 24분 전 윈도우 내
            } else if (premiereFinished) {
                // 방영 상태 체크
                Anime anime = record.anime;
                if (anime.getStatus() == AnimeStatus.NOW_SHOWING && record.isLastEpisode) {
                    anime.setStatus(AnimeStatus.ENDED);
                }

                // 1분 전 윈도우 내
            } else {
                Episode episode = record.episode;
                episode.setEvaluateState(EpEvaluateState.VOTING_WINDOW);

                // 방영 상태 체크
                Anime anime = record.anime;
                if (anime.getStatus() == AnimeStatus.UPCOMING && record.isFirstEpisode) {
                    anime.setStatus(AnimeStatus.NOW_SHOWING);

                    // AnimeCandidate 합류 로직 등 .. 추후 필요할 때 구현
                }
            }
        });
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
                    boolean isLastEpisode = i == (totalEpisodes - 1);
                    episodes.add(Episode.create(
                            saved,
                            i + 1,
                            scheduledAt,
                            nextEpScheduledAt,
                            isLastEpisode
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
    public EpisodeResponseDto.EpisodeResultDto updateTotalEpisodes(Long animeId, EpisodeRequestDto request) {
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

            List<Episode> episodes = episodeRepository.findAllByAnime_IdOrderByScheduledAtAsc(animeId);

            if (oldTotalEpisodes.equals(newTotalEpisodes)) {
                anime.updateTotalEpisodes(12);
                return EpisodeResponseDto.EpisodeResultDto.builder()
                        .message("에피소드 수 변화 없음")
                        .build();

            } else if (oldTotalEpisodes < newTotalEpisodes) {
                anime.updateTotalEpisodes(newTotalEpisodes);
                Episode lastEpisode = episodes.get(episodes.size() - 1);

                int diff = newTotalEpisodes - oldTotalEpisodes;
                LocalDateTime scheduledAt = lastEpisode.getNextEpScheduledAt();

                LocalDateTime nextEpScheduledAt;
                List<EpisodeResponseDto.EpisodePreviewDto> addedEpisodes = new ArrayList<>();
                for (int i = 0; i < diff; i++) {
                    int episodeNumber = lastEpisodeNumber + i + 1;
                    nextEpScheduledAt = scheduledAt.plusWeeks(1);
                    boolean isLastEpisode = i == (diff - 1);
                    Episode episode = Episode.create(
                            anime,
                            episodeNumber,
                            scheduledAt,
                            nextEpScheduledAt,
                            isLastEpisode
                    );
                    Episode saved = episodeRepository.save(episode);

                    animeCommentRepository.findAllByAnime_IdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                                    anime.getId(), scheduledAt, nextEpScheduledAt)
                            .forEach(ac -> ac.setEpisode(saved));

                    addedEpisodes.add(EpisodeResponseDto.EpisodePreviewDto.of(episode));

                    scheduledAt = scheduledAt.plusWeeks(1);
                }
                return EpisodeResponseDto.EpisodeResultDto.builder()
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

                List<EpisodeResponseDto.EpisodePreviewDto> deletedEpisodes = new ArrayList<>();
                int episodeNumber = newLastEpisodeNumber + 1;
                for (Long episodeId : episodeIdsToDelete) {
                    List<AnimeComment> comments = animeCommentRepository.findAllByEpisode_Id(episodeId);

                    // 댓글에서 끊어야 할 에피소드 관계 끊기
                    comments.forEach(ac -> ac.setEpisode(null));

                    deletedEpisodes.add(
                            EpisodeResponseDto.EpisodePreviewDto.builder()
                                    .episodeId(episodeId)
                                    .episodeNumber(episodeNumber)
                                    .build()
                    );
                    episodeNumber += 1;
                }
                episodeRepository.deleteAllById(episodeIdsToDelete);

                int diff = oldTotalEpisodes - newTotalEpisodes;
                return EpisodeResponseDto.EpisodeResultDto.builder()
                        .message("에피소드 삭제: " + diff + "개")
                        .deletedEpisodes(deletedEpisodes)
                        .build();
            }

        } finally {
            scheduleState.stopAdminMode();
        }
    }

    @Transactional
    public EpisodeResponseDto.EpisodeResultDto setUnknown(Long animeId) {
        if (scheduleState.isWeeklyScheduleRunning()) {
            throw new WeekHandler(ErrorStatus.SCHEDULE_RUNNING);
        }

        scheduleState.startAdminMode();
        try {
            Anime anime = animeRepository.findById(animeId)
                    .orElseThrow(() -> new AnimeHandler(ErrorStatus.ANIME_NOT_FOUND));

            anime.updateTotalEpisodes(null);

            return EpisodeResponseDto.EpisodeResultDto.builder()
                    .message("에피소드 수 미정")
                    .build();

        } finally {
            scheduleState.stopAdminMode();
        }
    }

    @Transactional
    public void breakEpisode(Long animeId, Long episodeId) {
//        Episode brokenEp = episodeRepository.findById(episodeId).orElseThrow(() ->
//                new EpisodeHandler(ErrorStatus.EPISODE_NOT_FOUND));
//
//        //=== 휴방으로 셋팅 ===//
//        brokenEp.setIsBreak(true);
//        LocalDateTime scheduledAt = brokenEp.getScheduledAt();
//
//        List<Episode> episodes = episodeRepository
//                .findAllByAnime_IdOrderByScheduledAtAsc(animeId);
//
//        int idx = 0;
//        for (Episode episode : episodes) {
//            if (episode.getScheduledAt().equals(scheduledAt)) break;
//            idx += 1;
//        }
//
//        //=== 남은 에피소드 한 주씩 미루기 ===//
//        Integer episodeNumber = brokenEp.getEpisodeNumber();
//        for (int i = idx + 1; i < episodes.size(); i++) {
//            episodes.get(i).setEpisodeNumber(episodeNumber);
//            episodeNumber += 1;
//        }
//
//        // 1.
//        LocalDateTime lastScheduledAt = episodes.get(episodes.size() - 1).getNextEpScheduledAt();
//        Episode episode = Episode.create(
//                animeRepository.findById(animeId).orElseThrow(() -> new AnimeHandler(ErrorStatus.ANIME_NOT_FOUND)),
//                episodeNumber,
//                lastScheduledAt,
//                lastScheduledAt.plusWeeks(1),
//                isLastEpisode
//        );
//
//        // 2.
//        episodeRepository.save(episode);
    }
}
