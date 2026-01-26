package com.duckstar.service;

import com.duckstar.apiPayload.exception.handler.EpisodeHandler;
import com.duckstar.domain.Anime;
import com.duckstar.domain.Member;
import com.duckstar.domain.Ott;
import com.duckstar.domain.enums.AdminTaskType;
import com.duckstar.domain.enums.OttType;
import com.duckstar.domain.mapping.comment.AnimeComment;
import com.duckstar.domain.mapping.weeklyVote.Episode;
import com.duckstar.fixture.AnimeFixture;
import com.duckstar.repository.AnimeComment.AnimeCommentRepository;
import com.duckstar.repository.AnimeRepository;
import com.duckstar.repository.Episode.EpisodeRepository;
import com.duckstar.repository.OttRepository;
import com.duckstar.security.domain.enums.OAuthProvider;
import com.duckstar.security.repository.MemberRepository;
import com.duckstar.service.AnimeService.AnimeCommandService;
import com.duckstar.service.EpisodeService.EpisodeCommandService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.duckstar.web.dto.EpisodeResponseDto.*;
import static com.duckstar.web.dto.admin.AdminLogDto.*;
import static com.duckstar.web.dto.admin.ContentResponseDto.*;
import static com.duckstar.web.dto.admin.EpisodeRequestDto.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
public class EpisodeServiceTest {

    @Autowired OttRepository ottRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired AnimeRepository animeRepository;
    @Autowired EpisodeRepository episodeRepository;
    @Autowired AnimeCommentRepository animeCommentRepository;

    @Autowired AnimeCommandService animeCommandService;
    @Autowired EpisodeCommandService episodeCommandService;

    Long memberId;

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
    public void modifyEpisode_테스트() throws Exception {
        //given
        Long animeId = animeCommandService.createAnime(
                memberId, AnimeFixture.tvaRequestBuilder().build());

        List<Episode> episodes = episodeRepository
                .findEpisodesByReleaseOrderByAnimeId(animeId);

        Episode targetEp = episodes.get(2);  // 세 번째 에피소드
        int newEpisodeNumber = 2;
        LocalDateTime rescheduledAt = targetEp.getScheduledAt().plusMinutes(30);
        ModifyRequestDto request = ModifyRequestDto.builder()
                .episodeNumber(newEpisodeNumber)
                .rescheduledAt(rescheduledAt)
                .build();

        //=== when(1) - rescheduledAt 앞뒤 간격 문제 없음 ===//
        List<ManagerProfileDto> profileDtos = episodeCommandService
                .modifyEpisode(memberId, targetEp.getId(), request);

        //then(1)
        episodes = episodeRepository
                .findEpisodesByReleaseOrderByAnimeId(animeId);
        Episode updatedTarget = episodes.get(2);

        List<ManagerProfileDto> profileDtosInTest = profileDtos.stream()
                .filter(dto -> dto.getTaskType() == AdminTaskType.EPISODE_MODIFY_NUMBER ||
                        dto.getTaskType() == AdminTaskType.EPISODE_RESCHEDULE)
                .toList();
        assertThat(profileDtosInTest).hasSize(2);

        assertThat(updatedTarget.getEpisodeNumber()).isEqualTo(newEpisodeNumber);
        
        assertThat(episodes.get(1).getNextEpScheduledAt()).isEqualTo(rescheduledAt);
        assertThat(updatedTarget.getScheduledAt()).isEqualTo(rescheduledAt);
        assertThat(updatedTarget.getIsRescheduled()).isTrue();

        //=== when(2) - rescheduledAt이 이전 회차를 침범 ===//
        Episode before = episodes.get(1);

        EpisodeHandler exception = assertThrows(EpisodeHandler.class, () -> {
            ModifyRequestDto crossLineRequest = ModifyRequestDto.builder()
                    .rescheduledAt(before.getScheduledAt().plusMinutes(24).minusNanos(1))
                    .build();
            episodeCommandService.modifyEpisode(memberId, updatedTarget.getId(), crossLineRequest);
        });

        //then(2)
        assertThat(exception.getErrorReason().getCode()).isEqualTo("EPISODE4003");

        //=== when(3) - rescheduledAt이 다음 회차를 침범 ===//
        Episode after = episodes.get(3);

        exception = assertThrows(EpisodeHandler.class, () -> {
            ModifyRequestDto crossLineRequest = ModifyRequestDto.builder()
                    .rescheduledAt(after.getScheduledAt())
                    .build();
            episodeCommandService.modifyEpisode(memberId, updatedTarget.getId(), crossLineRequest);
        });

        //then(2)
        assertThat(exception.getErrorReason().getCode()).isEqualTo("EPISODE4003");
    }

    @Test
    @Transactional
    public void breakEpisode_테스트() throws Exception {
        //given
        Long animeId = animeCommandService.createAnime(
                memberId, AnimeFixture.tvaRequestBuilder().totalEpisodes(12).build());

        List<Episode> episodes = episodeRepository
                .findEpisodesByReleaseOrderByAnimeId(animeId);

        Episode targetEp = episodes.get(1);  // 두 번째 에피소드 (2화)

        // 에피소드와 연결되지 않았지만, 방영 종료 후 한 주 차이로 생성된 댓글
        LocalDateTime commentCreatedAt = episodes.get(episodes.size() - 1).getNextEpScheduledAt();
        AnimeComment comment = animeCommentRepository.save(AnimeComment.create(
                animeRepository.getReferenceById(animeId),
                null,
                memberRepository.getReferenceById(memberId),
                false,
                0,
                null,
                "재밌어요"
        ));
        // Reflection을 사용하여 private 필드인 createdAt에 값 주입
        ReflectionTestUtils.setField(comment, "createdAt", commentCreatedAt);

        //when
        EpisodeManageResultDto result = episodeCommandService
                .breakEpisode(memberId, targetEp.getId());

        //then
        Anime anime = animeRepository.findById(animeId).get();
        // totalEpisodes 는 여전히 12개
        assertThat(anime.getTotalEpisodes()).isEqualTo(12);

        episodes = episodeRepository
                .findEpisodesByReleaseOrderByAnimeId(animeId);
        // 실제 에피소드 수는 13개 (휴방 포함)
        assertThat(episodes).hasSize(13);

        Episode updatedTarget = episodes.get(1);
        assertThat(updatedTarget.getIsBreak()).isTrue();

        // before 와 after 연결 검사
        assertThat(episodes.get(0).getNextEpScheduledAt())
                .isEqualTo(episodes.get(2).getScheduledAt());

        // 댓글 연관관계 셋팅 여부 검사
        Episode lastEpisode = episodes.get(episodes.size() - 1);
        assertThat(comment.getEpisode().getId()).isEqualTo(lastEpisode.getId());

        for (int i = 2; i < episodes.size(); i++) {
            // 에피소드 번호가 한 주씩 미뤄졌으므로 index(i) == episodeNumber(i)
            assertThat(episodes.get(i).getEpisodeNumber()).isEqualTo(i);

            if (i == episodes.size() - 1) {
                assertThat(episodes.get(i).getIsLastEpisode()).isTrue();
            } else {
                assertThat(episodes.get(i).getIsLastEpisode()).isFalse();
                assertThat(episodes.get(i).getNextEpScheduledAt())
                        .isEqualTo(episodes.get(i + 1).getScheduledAt());
            }
        }

        List<EpisodeDto> addedEpisodes = result.getEpisodeResultDto().getAddedEpisodes();
        assertThat(addedEpisodes).hasSize(1);
        assertThat(addedEpisodes.get(0).getEpisodeId()).isEqualTo(lastEpisode.getId());
        assertThat(result.getEpisodeResultDto().getDeletedEpisodes()).isNull();

        assertThat(result.getManagerProfileDto().getTaskType())
                .isEqualTo(AdminTaskType.EPISODE_BREAK);
    }

    @Test
    @Transactional
    public void deleteEpisode_테스트() throws Exception {
        //given
        Long animeId = animeCommandService.createAnime(
                memberId, AnimeFixture.tvaRequestBuilder().totalEpisodes(12).build());

        List<Episode> episodes = episodeRepository
                .findEpisodesByReleaseOrderByAnimeId(animeId);

        //=== when (1) - 다음 주 등의 미래 에피소드가 target ===//
        LocalDateTime nowForTest = LocalDateTime.of(2026, 1, 5,
                18, 0);

        Episode targetEp = episodes.get(1);  // 두 번째 에피소드 (2화) 26-01-12 20:00

        ManagerProfileDto profileDto = episodeCommandService
                .deleteMoreThanNextWeekEpisode(memberId, targetEp.getId(), nowForTest);

        //then (1)
        Anime anime = animeRepository.findById(animeId).get();
        // totalEpisodes 감소
        assertThat(anime.getTotalEpisodes()).isEqualTo(11);

        // 실제 에피소드 수 감소
        episodes = episodeRepository
                .findEpisodesByReleaseOrderByAnimeId(animeId);
        assertThat(episodes).hasSize(11);

        // targetEp 없음 확인
        Optional<Episode> optional = episodes.stream()
                .filter(e -> e.getId().equals(targetEp.getId()))
                .findFirst();
        assertThat(optional.isEmpty()).isTrue();

        // before 와 after 연결 검사
        assertThat(episodes.get(0).getNextEpScheduledAt())
                .isEqualTo(episodes.get(1).getScheduledAt());

        for (int i = 1; i < episodes.size(); i++) {
            // 에피소드 번호와 시간이 한 주씩 당겨졌으므로
            int episodeNumber = i + 1;
            assertThat(episodes.get(i).getEpisodeNumber()).isEqualTo(episodeNumber);

            if (i == episodes.size() - 1) {
                assertThat(episodes.get(i).getIsLastEpisode()).isTrue();
            } else {
                assertThat(episodes.get(i).getIsLastEpisode()).isFalse();
                assertThat(episodes.get(i).getNextEpScheduledAt())
                        .isEqualTo(episodes.get(i + 1).getScheduledAt());
            }
        }

        assertThat(profileDto.getTaskType())
                .isEqualTo(AdminTaskType.FUTURE_EPISODE_DELETE);

        //=== when (2) - 이번 주 또는 과거 에피소드가 target ===//
        LocalDateTime newNowForTest = LocalDateTime.of(2026, 1, 19,
                18, 0);

        // 이번 주 에피소드
        Episode newTargetEp = episodes.get(1);  // 두 번째 에피소드 (2화) 26-01-19 20:00

        EpisodeHandler exception = assertThrows(EpisodeHandler.class, () -> {
            episodeCommandService
                    .deleteMoreThanNextWeekEpisode(memberId, newTargetEp.getId(), newNowForTest);
        });

        // then (2)
        assertThat(exception.getErrorReason().getCode()).isEqualTo("EPISODE4004");
    }

    @Test
    public void queueEpisode_테스트() throws Exception {
        //given


        //when

        //then
    }
}
