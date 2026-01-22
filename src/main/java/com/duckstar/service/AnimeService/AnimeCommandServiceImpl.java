package com.duckstar.service.AnimeService;

import com.duckstar.abroad.reader.CsvImportService;
import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.AnimeHandler;
import com.duckstar.apiPayload.exception.handler.EpisodeHandler;
import com.duckstar.apiPayload.exception.handler.MemberHandler;
import com.duckstar.domain.Anime;
import com.duckstar.domain.Member;
import com.duckstar.domain.Ott;
import com.duckstar.domain.Quarter;
import com.duckstar.domain.enums.*;
import com.duckstar.domain.mapping.AdminActionLog;
import com.duckstar.domain.mapping.AnimeOtt;
import com.duckstar.domain.mapping.AnimeQuarter;
import com.duckstar.domain.mapping.weeklyVote.Episode;
import com.duckstar.repository.AnimeOtt.AnimeOttRepository;
import com.duckstar.repository.AnimeRepository;
import com.duckstar.repository.AnimeQuarter.AnimeQuarterRepository;
import com.duckstar.repository.Episode.EpisodeRepository;
import com.duckstar.repository.OttRepository;
import com.duckstar.s3.S3Uploader;
import com.duckstar.security.repository.MemberRepository;
import com.duckstar.service.AdminActionLogService;
import com.duckstar.service.CommentService;
import com.duckstar.service.QuarterService;
import com.duckstar.web.dto.OttDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.duckstar.domain.enums.DayOfWeekShort.*;
import static com.duckstar.util.QuarterUtil.*;
import static com.duckstar.web.dto.EpisodeResponseDto.*;
import static com.duckstar.web.dto.admin.AnimeRequestDto.*;
import static com.duckstar.web.dto.admin.ContentResponseDto.*;
import static com.duckstar.web.dto.admin.EpisodeRequestDto.*;

@Service
@RequiredArgsConstructor
@Transactional
public class AnimeCommandServiceImpl implements AnimeCommandService {
    private final AnimeQuarterRepository animeQuarterRepository;
    private final AnimeRepository animeRepository;
    private final EpisodeRepository episodeRepository;
    private final AnimeOttRepository animeOttRepository;
    private final MemberRepository memberRepository;

    private final CsvImportService csvImportService;
    private final QuarterService quarterService;
    private final OttRepository ottRepository;
    private final S3Uploader s3Uploader;
    private final CommentService commentService;
    private final AdminActionLogService adminActionLogService;

    public record PremieredEpRecord(
            Episode episode,
            Boolean isFirstEpisode,
            Boolean isLastEpisode,

            // 정책
            Boolean isAfter24Minutes,
            Boolean isAfter36Hours,

            Anime anime
    ) {}

    @Override
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

    @Override
    public Long addAnime(PostRequestDto request) throws IOException {
        LocalDateTime premiereDateTime = request.getPremiereDateTime();

        Integer totalEpisodes = request.getTotalEpisodes();
        boolean isTVA = request.getMedium() == Medium.TVA;
        if (isTVA && totalEpisodes == null) {
            totalEpisodes = 12;
        }

        //=== 방영 여부 결정 ===//
        AnimeStatus status = AnimeStatus.UPCOMING;
        LocalDateTime lastEpScheduledAt = null;
        if (premiereDateTime != null) {
            LocalDateTime now = LocalDateTime.now();

            if (!now.isBefore(premiereDateTime)) {
                status = AnimeStatus.NOW_SHOWING;
            }

            if (isTVA) {
                lastEpScheduledAt = premiereDateTime.plusWeeks(totalEpisodes - 1);
                if (!now.isBefore(lastEpScheduledAt.plusMinutes(24))) status = AnimeStatus.ENDED;
            }
        }

        Map<SiteType, String> officialSite = Map.of();
        // officialSite String 파싱
        if (request.getOfficialSiteString() != null) {
            ObjectMapper mapper = new ObjectMapper();
            officialSite = mapper.readValue(
                    request.getOfficialSiteString(),
                    new TypeReference<>() {}
            );
        }

        //=== 애니메이션 저장 ===//
        DayOfWeekShort dayOfWeek = request.getDayOfWeek();
        LocalTime airTime = request.getAirTime();

        Anime anime = Anime.builder()
                .titleKor(request.getTitleKor())
                .titleOrigin(request.getTitleOrigin())
                .titleEng(request.getTitleEng())

                .medium(request.getMedium())

                .status(status)
                .airTime(airTime)
                .premiereDateTime(premiereDateTime)
                .dayOfWeek(dayOfWeek)
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
            //=== premiereDateTime의 분기 연관관계 생성 ===//
            YQWRecord firstEpWeekRecord = getThisWeekRecord(premiereDateTime);

            // 방영일이 특정 분기의 12주차 이상이면 다음 분기 애니메이션으로 간주
            boolean isFirstQuarterExcluded = firstEpWeekRecord.weekValue() >= 12;
            if (isFirstQuarterExcluded) {
                firstEpWeekRecord = firstEpWeekRecord.getNextQuarterRecord();
            }
            YQWRecord lastEpWeekRecord = null;

            //=== 에피소드 생성 ===//
            if (totalEpisodes != null) {
                List<Episode> episodes = new ArrayList<>();

                LocalDateTime scheduledAt = premiereDateTime;
                LocalDateTime nextEpScheduledAt = scheduledAt.plusWeeks(1);
                if (dayOfWeek.getValue() > 7 || airTime == null) {  // 방향(dayOfWeek, airTime) 없을 때
                    for (int i = 0; i < totalEpisodes; i++) {
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
                } else {  // 방향 존재
                    nextEpScheduledAt = LocalDateTime.of(
                            adjustDateByDayOfWeek(premiereDateTime.plusWeeks(1), dayOfWeek),
                            airTime
                    );
                    episodes.add(Episode.create(
                            saved,
                            1,
                            scheduledAt,
                            nextEpScheduledAt,
                            false
                    ));

                    for (int i = 1; i < totalEpisodes; i++) {
                        scheduledAt = nextEpScheduledAt;
                        nextEpScheduledAt = scheduledAt.plusWeeks(1);
                        boolean isLastEpisode = i == (totalEpisodes - 1);
                        episodes.add(Episode.create(
                                saved,
                                i + 1,
                                scheduledAt,
                                nextEpScheduledAt,
                                isLastEpisode
                        ));
                    }
                }
                episodeRepository.saveAll(episodes);

                //=== lastEpScheduledAt에 따라 존재하는 모든 소속 분기 추가 ===//
                lastEpWeekRecord = getThisWeekRecord(lastEpScheduledAt);
            }

            List<Quarter> quarters = quarterService
                    .getOrCreateQuartersByEdges(firstEpWeekRecord, lastEpWeekRecord);

            List<AnimeQuarter> additionalAnimeQuarters = quarters.stream()
                    .map(q -> AnimeQuarter.create(saved, q))
                    .toList();
            List<AnimeQuarter> animeQuarters = new ArrayList<>(additionalAnimeQuarters);

            animeQuarterRepository.saveAll(animeQuarters);
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
            List<AnimeOtt> animeOtts = ottDtos.stream()
                    .map(ottDto ->
                            AnimeOtt.create(
                                    saved,
                                    ottMap.get(ottDto.getOttType()),
                                    ottDto.getWatchUrl()
                            ))
                    .toList();
            animeOttRepository.saveAll(animeOtts);
        }

        return saved.getId();
    }

    @Override
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

    @Override
    public EpisodeManageResultDto updateTotalEpisodes(
            Long memberId,
            Long animeId,
            TotalEpisodesRequestDto request
    ) {
        Member member = memberRepository.findById(memberId).orElseThrow(() ->
                new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        Anime anime = animeRepository.findById(animeId)
                .orElseThrow(() -> new AnimeHandler(ErrorStatus.ANIME_NOT_FOUND));

        int oldTotal = (anime.getTotalEpisodes() == null) ? 12 : anime.getTotalEpisodes();
        int newTotal = request.getTotalEpisodes();

        if (oldTotal == newTotal) {
            return EpisodeManageResultDto.builder()
                    .build();
        }

        List<Episode> episodes = episodeRepository.findEpisodesByReleaseOrderByAnimeId(animeId);
        List<Episode> addedEpisodes = new ArrayList<>();
        List<Episode> deletedEpisodes = new ArrayList<>();

        if (oldTotal < newTotal) {
            anime.setTotalEpisodes(newTotal);

            Episode oldLastEpisode = episodes.get(episodes.size() - 1);
            oldLastEpisode.setIsLastEpisode(false);

            //=== 애니메이션의 dayOfWeek, airTime 기준 시각으로 에피소드 생성 ===//
            DayOfWeekShort dayOfWeek = anime.getDayOfWeek();
            LocalTime airTime = anime.getAirTime();
            if (dayOfWeek.getValue() > 7 || airTime == null) {
                throw new AnimeHandler(ErrorStatus.TVA_DIRECTION_NOT_SET);
            }

            LocalDateTime nextEpScheduledAt = oldLastEpisode.getNextEpScheduledAt();
            LocalDateTime scheduledAt = LocalDateTime.of(
                    adjustDateByDayOfWeek(nextEpScheduledAt, dayOfWeek),
                    airTime
            );

            int diff = newTotal - oldTotal;
            for (int i = 0; i < diff; i++) {
                int episodeNumber = oldTotal + i + 1;
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

                addedEpisodes.add(saved);

                scheduledAt = nextEpScheduledAt;
            }

        } else {
            anime.setTotalEpisodes(newTotal);

            // 삭제되어야 하는 에피소드들
            deletedEpisodes = episodes.stream()
                    .filter(ep -> ep.getEpisodeNumber() > newTotal)
                    .toList();

            if (deletedEpisodes.stream().anyMatch(ep -> ep.getVoterCount() > 0)) {
                throw new EpisodeHandler(ErrorStatus.CANNOT_DELETE_EPISODE);
            }

            episodeRepository.deleteAllInBatch(deletedEpisodes);

            episodes.stream()
                    .filter(ep ->
                            ep.getEpisodeNumber().equals(newTotal) &&
                                    !ep.getIsBreak()
                    )
                    .findFirst()
                    .ifPresent(ep -> ep.setIsLastEpisode(true));
        }

        //=== 댓글 연관관계 재설정 및 로그 기록 ===//
        commentService.redefineRelationWithTails(
                animeId, addedEpisodes, deletedEpisodes);

        AdminActionLog adminActionLog = adminActionLogService.saveAdminActionLog(
                member, anime, AdminTaskType.ANIME_EPISODE_TOTAL_COUNT);

        return EpisodeManageResultDto.toResultDto(
                addedEpisodes.stream().map(EpisodeDto::of).toList(),
                deletedEpisodes.stream().map(EpisodeDto::of).toList(),
                member,
                adminActionLog
        );
    }

    @Override
    public EpisodeManageResultDto setUnknown(Long memberId, Long animeId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() ->
                new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        Anime anime = animeRepository.findById(animeId)
                .orElseThrow(() -> new AnimeHandler(ErrorStatus.ANIME_NOT_FOUND));

        anime.setTotalEpisodes(null);

        // 로그 기록
        AdminActionLog adminActionLog = adminActionLogService.saveAdminActionLog(
                member, anime, AdminTaskType.ANIME_EPISODE_TOTAL_COUNT);

        return EpisodeManageResultDto.toResultDto(
                null,
                null,
                member,
                adminActionLog
        );
    }
}
