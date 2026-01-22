package com.duckstar.service.EpisodeService;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.AnimeHandler;
import com.duckstar.apiPayload.exception.handler.EpisodeHandler;
import com.duckstar.apiPayload.exception.handler.MemberHandler;
import com.duckstar.domain.Anime;
import com.duckstar.domain.Member;
import com.duckstar.domain.enums.AdminTaskType;
import com.duckstar.domain.enums.DayOfWeekShort;
import com.duckstar.domain.mapping.AdminActionLog;
import com.duckstar.domain.mapping.weeklyVote.Episode;
import com.duckstar.repository.AnimeComment.AnimeCommentRepository;
import com.duckstar.repository.AnimeRepository;
import com.duckstar.repository.Episode.EpisodeRepository;
import com.duckstar.security.repository.MemberRepository;
import com.duckstar.service.AdminActionLogService;
import com.duckstar.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static com.duckstar.domain.enums.DayOfWeekShort.adjustDateByDayOfWeek;
import static com.duckstar.web.dto.EpisodeResponseDto.*;
import static com.duckstar.web.dto.admin.ContentResponseDto.*;
import static com.duckstar.web.dto.admin.EpisodeRequestDto.*;

@Service
@RequiredArgsConstructor
@Transactional
public class EpisodeCommandServiceImpl implements EpisodeCommandService {
    private final EpisodeRepository episodeRepository;
    private final MemberRepository memberRepository;
    private final AnimeCommentRepository animeCommentRepository;

    private final CommentService commentService;
    private final AdminActionLogService adminActionLogService;

    final int MIN_EPISODE_GAP_MINUTES = 24;
    private final AnimeRepository animeRepository;

    @Override
    public void updateAllEpisodeStates() {
        LocalDateTime now = LocalDateTime.now();
        List<Episode> allEpisodes = episodeRepository.findAll();
        allEpisodes.forEach(episode -> episode.updateEvaluateState(now));
    }

    @Override
    public EpisodeManageResultDto modifyEpisode(
            Long memberId,
            Long episodeId,
            ModifyRequestDto request
    ) {
        Member member = memberRepository.findById(memberId).orElseThrow(() ->
                new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        Episode targetEp = episodeRepository.findById(episodeId).orElseThrow(() ->
                new EpisodeHandler(ErrorStatus.EPISODE_NOT_FOUND));

        AdminActionLog adminActionLog = null;

        //=== 에피소드 회차 수정 ===//
        Integer episodeNumber = request.getEpisodeNumber();
        if (episodeNumber != null) {
            targetEp.setEpisodeNumber(episodeNumber);

            // 로그 기록
            adminActionLog = adminActionLogService.saveAdminActionLog(
                    member, targetEp, AdminTaskType.EPISODE_MODIFY_NUMBER);
        }

        //=== 에피소드 방영 시간 수정 ===//
        LocalDateTime rescheduledAt = request.getRescheduledAt();
        if (rescheduledAt != null) {
            List<Episode> episodes = episodeRepository.findEpisodesByReleaseOrderByAnimeId(targetEp.getAnime().getId());
            int idx = episodes.indexOf(targetEp);

            // 시간 수정 및 앞뒤 간격 검증
            validateAndReschedule(episodes, idx, rescheduledAt);

            adminActionLog = adminActionLogService.saveAdminActionLog(
                    member, targetEp, AdminTaskType.EPISODE_RESCHEDULE);
        }

        return EpisodeManageResultDto.toResultDto(
                null,
                null,
                member,
                adminActionLog
        );
    }

    private void validateAndReschedule(List<Episode> episodes, int idx, LocalDateTime rescheduledAt) {
        // 이전 회차와의 간격 체크
        if (idx > 0) {
            Episode before = episodes.get(idx - 1);
            checkGap(before.getScheduledAt(), rescheduledAt);
            // 이전 회차의 다음 방영 시간 업데이트
            before.setNextEpScheduledAt(rescheduledAt);
        }

        // 다음 회차와의 간격 체크
        if (idx < episodes.size() - 1) {
            Episode after = episodes.get(idx + 1);
            checkGap(rescheduledAt, after.getScheduledAt());
        }

        // 검증 통과 후 실제 시간 수정 + evaluateState 변경
        episodes.get(idx).reschedule(rescheduledAt);
    }

    private void checkGap(LocalDateTime start, LocalDateTime end) {
        long minutes = Duration.between(start, end).toMinutes();
        if (minutes < MIN_EPISODE_GAP_MINUTES) {
            throw new EpisodeHandler(ErrorStatus.INVALID_RESCHEDULE_TIME);
        }
    }

    @Override
    public EpisodeManageResultDto breakEpisode(Long memberId, Long episodeId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() ->
                new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        Episode brokenEp = episodeRepository.findById(episodeId).orElseThrow(() ->
                new EpisodeHandler(ErrorStatus.EPISODE_NOT_FOUND));

        Anime anime = brokenEp.getAnime();
        Long animeId = anime.getId();

        //=== 휴방으로 셋팅 ===//
        brokenEp.breakEpisode();
        List<Episode> episodes = episodeRepository.findEpisodesByReleaseOrderByAnimeId(animeId);

        int idx = episodes.indexOf(brokenEp);

        // 이전 에피소드의 nextEpScheduledAt 수정
        if (idx > 0) {
            LocalDateTime nextEpScheduledAt = brokenEp.getNextEpScheduledAt();
            episodes.get(idx - 1).setNextEpScheduledAt(nextEpScheduledAt);
        }

        //=== 남은 회차들 번호만 당기기 ===//
        Integer episodeNumber = brokenEp.getEpisodeNumber();
        for (int i = idx + 1; i < episodes.size(); i++) {
            Episode current = episodes.get(i);
            current.setEpisodeNumber(episodeNumber);
            episodeNumber += 1;
        }

        // 휴방에 의해 전체 개수가 하나 줄었으므로 끝 에피소드 하나 더 생성
        Episode oldLast = episodes.get(episodes.size() - 1);
        oldLast.setIsLastEpisode(false);

        LocalDateTime lastScheduledAt = oldLast.getNextEpScheduledAt();
        Episode newLast = Episode.create(
                anime,
                episodeNumber,
                lastScheduledAt,
                lastScheduledAt.plusWeeks(1),
                true
        );
        Episode saved = episodeRepository.save(newLast);

        //=== 댓글 연관관계 재설정 및 로그 기록 ===//
        commentService.redefineRelationWithTails(
                animeId, List.of(saved), null);

        AdminActionLog adminActionLog = adminActionLogService.saveAdminActionLog(
                member, brokenEp, AdminTaskType.EPISODE_BREAK);

        return EpisodeManageResultDto.toResultDto(
                List.of(EpisodeDto.of(saved)),
                null,
                member,
                adminActionLog
        );
    }

    @Override
    public EpisodeManageResultDto deleteEpisode(Long memberId, Long episodeId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() ->
                new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        Episode targetEp = episodeRepository.findById(episodeId).orElseThrow(() ->
                new EpisodeHandler(ErrorStatus.EPISODE_NOT_FOUND));

        if (targetEp.getVoterCount() > 0 || animeCommentRepository.existsByEpisode(targetEp)) {
            throw new EpisodeHandler(ErrorStatus.CANNOT_DELETE_EPISODE);
        }

        Anime anime = targetEp.getAnime();
        Long animeId = anime.getId();

        //=== 에피소드 삭제를 위한 일정 변경 ===//
        List<Episode> episodes = episodeRepository.findEpisodesByReleaseOrderByAnimeId(animeId);
        int idx = episodes.indexOf(targetEp);
        if (idx == episodes.size() - 1) {
            /* tail 이면 단순 삭제 */
            if (idx > 0) {
                episodes.get(idx - 1).setIsLastEpisode(true);
            }
        } else {
            // tail이 아니면 뒤에 오는 회차 번호와 시간 모두 앞으로 당기기
            Integer episodeNumber = targetEp.getEpisodeNumber();
            LocalDateTime scheduledAt = targetEp.getScheduledAt();
            for (int i = idx + 1; i < episodes.size(); i++) {
                Episode current = episodes.get(i);
                current.setEpisodeNumber(episodeNumber);
                current.setScheduledAt(scheduledAt);
                current.setNextEpScheduledAt(scheduledAt.plusWeeks(1));

                episodeNumber += 1;
                scheduledAt = scheduledAt.plusWeeks(1);
            }
        }

        episodeRepository.delete(targetEp);

        // 애니메이션 totalEpisodes 변경
        Integer totalEpisodes = anime.getTotalEpisodes();
        if (totalEpisodes != null) anime.setTotalEpisodes(totalEpisodes - 1);

        // 로그 기록
        AdminActionLog adminActionLog = adminActionLogService.saveAdminActionLog(
                member, targetEp, AdminTaskType.EPISODE_DELETE);

        return EpisodeManageResultDto.toResultDto(
                null,
                List.of(EpisodeDto.of(targetEp)),
                member,
                adminActionLog
        );
    }

    @Override
    public EpisodeManageResultDto queueEpisode(Long memberId, Long animeId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() ->
                new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        Anime anime = animeRepository.findById(animeId).orElseThrow(() ->
                new AnimeHandler(ErrorStatus.ANIME_NOT_FOUND));

        int episodeNumber = 1;
        LocalDateTime scheduledAt;

        List<Episode> episodes = episodeRepository.findEpisodesByReleaseOrderByAnimeId(animeId);
        if (!episodes.isEmpty()) {
            DayOfWeekShort dayOfWeek = anime.getDayOfWeek();
            LocalTime airTime = anime.getAirTime();
            if (dayOfWeek.getValue() > 7 || airTime == null) {
                throw new AnimeHandler(ErrorStatus.TVA_DIRECTION_NOT_SET);
            }

            Episode oldLast = episodes.get(episodes.size() - 1);
            oldLast.setIsLastEpisode(false);

            episodeNumber = oldLast.getEpisodeNumber() + 1;
            scheduledAt = LocalDateTime.of(
                    adjustDateByDayOfWeek(oldLast.getNextEpScheduledAt(), dayOfWeek),
                    airTime
            );
        } else {
            // 에피소드가 없다면 첫 방영시간 기준의 1화부터 생성
            scheduledAt = anime.getPremiereDateTime();
        }

        Episode newLast = Episode.create(
                anime,
                episodeNumber,
                scheduledAt,
                scheduledAt.plusWeeks(1),
                true
        );
        Episode saved = episodeRepository.save(newLast);

        //=== 댓글 연관관계 재설정 및 로그 기록 ===//
        commentService.redefineRelationWithTails(
                animeId, List.of(saved), null);

        AdminActionLog adminActionLog = adminActionLogService.saveAdminActionLog(
                member, saved, AdminTaskType.EPISODE_CREATE);

        return EpisodeManageResultDto.toResultDto(
                List.of(EpisodeDto.of(saved)),
                null,
                member,
                adminActionLog
        );
    }
}
