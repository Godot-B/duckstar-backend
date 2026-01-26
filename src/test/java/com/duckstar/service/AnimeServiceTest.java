package com.duckstar.service;

import com.duckstar.apiPayload.exception.handler.EpisodeHandler;
import com.duckstar.domain.Anime;
import com.duckstar.domain.Member;
import com.duckstar.domain.Ott;
import com.duckstar.domain.enums.*;
import com.duckstar.domain.mapping.AdminActionLog;
import com.duckstar.domain.mapping.comment.AnimeComment;
import com.duckstar.domain.mapping.weeklyVote.Episode;
import com.duckstar.fixture.AnimeFixture;
import com.duckstar.repository.AdminActionLog.AdminActionLogRepository;
import com.duckstar.repository.AnimeComment.AnimeCommentRepository;
import com.duckstar.repository.AnimeOtt.AnimeOttRepository;
import com.duckstar.repository.AnimeQuarter.AnimeQuarterRepository;
import com.duckstar.repository.AnimeRepository;
import com.duckstar.repository.Episode.EpisodeRepository;
import com.duckstar.repository.OttRepository;
import com.duckstar.security.domain.enums.OAuthProvider;
import com.duckstar.security.repository.MemberRepository;
import com.duckstar.service.AnimeService.AnimeCommandService;
import com.duckstar.web.dto.OttDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static com.duckstar.domain.enums.DayOfWeekShort.adjustTimeByDirection;
import static com.duckstar.web.dto.AnimeResponseDto.*;
import static com.duckstar.web.dto.admin.AdminLogDto.*;
import static com.duckstar.web.dto.admin.AnimeRequestDto.*;
import static com.duckstar.web.dto.admin.ContentResponseDto.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
public class AnimeServiceTest {

    @Autowired OttRepository ottRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired AnimeRepository animeRepository;
    @Autowired EpisodeRepository episodeRepository;
    @Autowired AnimeQuarterRepository animeQuarterRepository;
    @Autowired AnimeOttRepository animeOttRepository;
    @Autowired AdminActionLogRepository adminActionLogRepository;
    @Autowired AnimeCommentRepository animeCommentRepository;

    @Autowired AnimeCommandService animeCommandService;

    Long memberId;;

    @BeforeEach
    void setUp() {
        List<Ott> otts = List.of(
                Ott.create(OttType.LAFTEL),
                Ott.create(OttType.NETFLIX),
                Ott.create(OttType.WAVVE),
                Ott.create(OttType.TVING),
                Ott.create(OttType.WATCHA),
                Ott.create(OttType.PRIME)
        );
        ottRepository.saveAll(otts);

        Member saved = memberRepository.save(Member.createSocial(
                OAuthProvider.KAKAO,
                "providerId",
                "nickname",
                null
        ));
        memberId = saved.getId();
    }

    @Test
    @Transactional
    public void createAnime_TVA_테스트() throws Exception {
        //given
        OttDto laftel = OttDto.builder()
                .ottType(OttType.LAFTEL)
                .watchUrl("laftel-url")
                .build();
        OttDto netflix = OttDto.builder()
                .ottType(OttType.NETFLIX)
                .watchUrl("netflix-url")
                .build();

        // premiereDateTime 과 다른 상황 설정
        // 1화: 1월 5일 -> 2화: 1월 12일 (X) 1월 17일 (O)
        DayOfWeekShort dayOfWeek = DayOfWeekShort.SAT;
        LocalTime airTime = LocalTime.of(21, 30);

        // 1월 5일(premiereDateTime) ~ 4월 25일
        // 1분기에선 12개 주, 2분기에선 4개 주 걸침
        //  -> 따라서 [1분기, 2분기] 애니메이션
        // 만약 15개 회차라면 2분기에선 3개 주가 걸치므로 2분기 애니로는 간주하지 X
        int totalEpisodes = 16;

        PostRequestDto request = AnimeFixture.tvaRequestBuilder()
                .dayOfWeek(dayOfWeek)
                .airTime(airTime)
                .totalEpisodes(totalEpisodes)
                .ottDtos(List.of(laftel, netflix))
                .build();

        //when
        Long animeId = animeCommandService.createAnime(memberId, request);

        //then
        // 1. Anime 생성 확인
        Anime anime = animeRepository.findById(animeId).get();
        assertThat(anime.getTitleKor()).isEqualTo("TVA_테스트");

        LocalDateTime premiereDateTime = anime.getPremiereDateTime();
        // 1-1. AnimeStatus 검사
        LocalDateTime lastEpScheduledAt = adjustTimeByDirection(
                premiereDateTime.plusWeeks(totalEpisodes - 1),
                dayOfWeek,
                airTime
        );
        // 도메인 비즈니스 로직을 함께 검사
        anime.setStatusWhenCreateByBase(premiereDateTime.minusNanos(1));
        assertThat(anime.getStatus()).isEqualTo(AnimeStatus.UPCOMING);

        LocalDateTime lastEpFinishedAt = lastEpScheduledAt.plusMinutes(24);

        anime.setStatusWhenCreateByBase(lastEpFinishedAt.minusNanos(1));
        assertThat(anime.getStatus()).isEqualTo(AnimeStatus.NOW_SHOWING);
        anime.setStatusWhenCreateByBase(lastEpFinishedAt);
        assertThat(anime.getStatus()).isEqualTo(AnimeStatus.ENDED);

        // 2. 생성된 에피소드의 scheduledAt 검사
        List<Episode> episodes = episodeRepository
                .findEpisodesByReleaseOrderByAnimeId(animeId);

        // 2-1. premiereDateTime 예외 검사
        LocalDateTime firstEpScheduledAt = episodes.get(0).getScheduledAt();
        System.out.println("firstEpScheduledAt: " + firstEpScheduledAt);

        assertThat(firstEpScheduledAt).isEqualTo(premiereDateTime);
        // 2-2. 방향 설정 유무 검사
        LocalDateTime secondEpScheduledAt = episodes.get(1).getScheduledAt();
        System.out.println("secondEpScheduledAt: " + secondEpScheduledAt);

        assertThat(secondEpScheduledAt.getDayOfWeek().getValue())
                .isEqualTo(dayOfWeek.getValue());
        assertThat(secondEpScheduledAt.toLocalTime()).isEqualTo(airTime);
        // 2-3. 마지막 화 검사
        System.out.println("lastEpScheduledAt: " + lastEpScheduledAt);
        assertThat(episodes.get(episodes.size() - 1).getScheduledAt())
                .isEqualTo(lastEpScheduledAt);

        // 3. 생성된 분기 연관관계 검사
        List<QuarterDto> quarterDtos = animeQuarterRepository
                .getQuarterDtosByAnimeId(animeId);

        assertThat(quarterDtos).hasSize(2);
        assertThat(quarterDtos.get(0).getYear()).isEqualTo(2026);
        assertThat(quarterDtos.get(0).getQuarter()).isEqualTo(1);
        assertThat(quarterDtos.get(1).getYear()).isEqualTo(2026);
        assertThat(quarterDtos.get(1).getQuarter()).isEqualTo(2);

        // 4. 생성된 OTT 연관관계 검사
        List<OttDto> ottDtos = animeOttRepository.getOttDtosByAnimeId(animeId);
        assertThat(ottDtos).hasSize(2);
        assertThat(ottDtos.get(0).getOttType()).isEqualTo(laftel.getOttType());
        assertThat(ottDtos.get(0).getWatchUrl()).isEqualTo(laftel.getWatchUrl());
        assertThat(ottDtos.get(1).getOttType()).isEqualTo(netflix.getOttType());
        assertThat(ottDtos.get(1).getWatchUrl()).isEqualTo(netflix.getWatchUrl());

        // 5. 생성된 로그 검사
        List<AdminActionLog> logs = adminActionLogRepository.findAllByAnime_Id(animeId);
        AdminActionLog adminActionLog = logs.get(0);
        assertThat(logs).hasSize(1);
        assertThat(adminActionLog.getMember().getId()).isEqualTo(memberId);
        assertThat(adminActionLog.getAdminTaskType()).isEqualTo(AdminTaskType.ANIME_CREATE);
    }

    //TODO createAnime_NONE_TVA_테스트()

    @Test
    @Transactional
    public void updateInfo_TVA_테스트() throws Exception {
        //given
        Long animeId = animeCommandService.createAnime(
                memberId, AnimeFixture.tvaRequestBuilder().build());
        Anime anime = animeRepository.findById(animeId).get();

        // 잘못된 상태 설정
        anime.setStatus(AnimeStatus.UPCOMING);

        // 현재는 7화 방영 주
        LocalDateTime nowForTest = LocalDateTime
                .of(2026, 2, 16, 18, 0);

        DayOfWeekShort newDayOfWeek = DayOfWeekShort.WED;
        LocalTime newAirTime = LocalTime.of(23, 30);
        InfoRequestDto request = InfoRequestDto.builder()
                // 수요일 23:30 으로 방향 변경
                .dayOfWeek(newDayOfWeek)
                .airTime(newAirTime)
                // 올바른 상태 설정
                .status(AnimeStatus.NOW_SHOWING)
                // 새로운 제작사명
                .corp("new-corp")
                .build();

        //when
        List<ManagerProfileDto> profileDtos = animeCommandService
                .updateInfo(memberId, animeId, request, nowForTest);

        //then
        List<ManagerProfileDto> profileDtosInTest = profileDtos.stream()
                .filter(dto -> dto.getTaskType() == AdminTaskType.ANIME_DIRECTION_UPDATE ||
                        dto.getTaskType() == AdminTaskType.ANIME_STATUS_UPDATE ||
                        dto.getTaskType() == AdminTaskType.ANIME_INFO_UPDATE)
                .toList();
        assertThat(profileDtosInTest).hasSize(3);

        assertThat(anime.getDayOfWeek()).isEqualTo(newDayOfWeek);
        assertThat(anime.getAirTime()).isEqualTo(newAirTime);

        List<Episode> modifiedEpisodes = episodeRepository
                .findEpisodesByReleaseOrderByAnimeId(animeId).stream()
                .filter(e -> e.getScheduledAt().getDayOfWeek().getValue() == newDayOfWeek.getValue())
                .toList();
        // 7화 방영 주는 제외하고, 다음 주부터의 에피소드들이 변경되었으므로
        assertThat(modifiedEpisodes).hasSize(5);

        assertThat(anime.getStatus()).isEqualTo(AnimeStatus.NOW_SHOWING);
        assertThat(anime.getCorp()).isEqualTo("new-corp");
    }

    @Test
    @Transactional
    public void updateTotalEpisodes_에피소드_추가_테스트() throws Exception {
        //given
        Long animeId = animeCommandService.createAnime(
                memberId, AnimeFixture.tvaRequestBuilder().build());

        // 12화까지 방영됨
        LocalDateTime nowForTest = LocalDateTime
                .of(2026, 3, 23, 20, 0);

        // 수요일 23:30 으로 방향 변경
        DayOfWeekShort newDayOfWeek = DayOfWeekShort.WED;
        LocalTime newAirTime = LocalTime.of(23, 30);

        InfoRequestDto infoRequest = InfoRequestDto.builder()
                .dayOfWeek(newDayOfWeek)
                .airTime(newAirTime)
                .build();

        animeCommandService.updateInfo(memberId, animeId, infoRequest, nowForTest);

        TotalEpisodesRequestDto totalEpisodesRequest = TotalEpisodesRequestDto.builder()
                .totalEpisodes(24)
                .build();

        //when
        EpisodeManageResultDto result = animeCommandService
                .updateTotalEpisodes(memberId, animeId, totalEpisodesRequest);

        //then
        List<Episode> episodes = episodeRepository
                .findEpisodesByReleaseOrderByAnimeId(animeId);
        assertThat(episodes).hasSize(24);

        // 1. 방향에 맞춰 생성된 에피소드 검사
        List<Episode> addedEpisodes = episodes.stream()
                .filter(e -> {
                    LocalDateTime scheduledAt = e.getScheduledAt();
                    return scheduledAt.getDayOfWeek().getValue() == newDayOfWeek.getValue() &&
                            scheduledAt.toLocalTime().equals(newAirTime);
                }).toList();
        assertThat(addedEpisodes).hasSize(12);

        assertThat(episodes.get(10).getNextEpScheduledAt())
                .isEqualTo(episodes.get(11).getScheduledAt());

        // 2. 추가된 에피소드로의 nextEpScheduledAt 전환 매끄러운지
        assertThat(episodes.get(11).getNextEpScheduledAt())
                .isEqualTo(addedEpisodes.get(0).getScheduledAt());

        // 3. 로그 검사
        assertThat(result.getManagerProfileDto().getTaskType())
                .isEqualTo(AdminTaskType.ANIME_EPISODE_TOTAL_COUNT);
    }

    @Test
    @Transactional
    public void updateTotalEpisodes_에피소드_삭제_테스트() throws Exception {
        //given
        Long animeId = animeCommandService.createAnime(
                memberId, AnimeFixture.tvaRequestBuilder().build());
        Anime anime = animeRepository.findById(animeId).get();

        List<Episode> episodes = episodeRepository
                .findEpisodesByReleaseOrderByAnimeId(animeId);

        // 7개 에피소드에 투표자 존재
        for (int i = 0; i < 7; i++) {
            Episode episode = episodes.get(i);
            episode.addVoterCount();
        }

        // 8번째 에피소드에 댓글 존재
        Episode episode = episodes.get(7);
        AnimeComment comment = animeCommentRepository.save(AnimeComment.create(
                anime,
                episode,
                memberRepository.getReferenceById(memberId),
                false,
                0,
                null,
                "재밌어요"
        ));

        //=== when (1) - 연관 없음 ===//
        TotalEpisodesRequestDto totalEpisodesRequest = TotalEpisodesRequestDto.builder()
                .totalEpisodes(8)
                .build();
        EpisodeManageResultDto result = animeCommandService
                .updateTotalEpisodes(memberId, animeId, totalEpisodesRequest);

        //then (1)
        episodes = episodeRepository
                .findEpisodesByReleaseOrderByAnimeId(animeId);
        assertThat(episodes).hasSize(8);

        // 로그 검사
        assertThat(result.getManagerProfileDto().getTaskType())
                .isEqualTo(AdminTaskType.ANIME_EPISODE_TOTAL_COUNT);

        //=== when (2) - 연관된 댓글 존재 ===//
        EpisodeHandler exception = assertThrows(EpisodeHandler.class, () -> {
            TotalEpisodesRequestDto totalEpisodesRequestDto = TotalEpisodesRequestDto.builder()
                    .totalEpisodes(7)
                    .build();
            animeCommandService.updateTotalEpisodes(
                    memberId,
                    animeId,
                    totalEpisodesRequestDto);
        });

        //then (2)
        assertThat(exception.getErrorReason().getCode()).isEqualTo("EPISODE4002");

        //=== when (3) - 투표자 존재 ===//
        animeCommentRepository.delete(comment);

        exception = assertThrows(EpisodeHandler.class, () -> {
            TotalEpisodesRequestDto totalEpisodesRequestDto = TotalEpisodesRequestDto.builder()
                    .totalEpisodes(6)
                    .build();
            animeCommandService.updateTotalEpisodes(
                    memberId,
                    animeId,
                    totalEpisodesRequestDto);
        });

        //then (3)
        assertThat(exception.getErrorReason().getCode()).isEqualTo("EPISODE4002");
    }
}
