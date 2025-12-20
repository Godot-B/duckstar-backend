package com.duckstar.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.AdminHandler;
import com.duckstar.apiPayload.exception.handler.MemberHandler;
import com.duckstar.apiPayload.exception.handler.WeekHandler;
import com.duckstar.domain.enums.AdminTaskType;
import com.duckstar.domain.mapping.AdminActionLog;
import com.duckstar.domain.mapping.weeklyVote.WeekVoteSubmission;
import com.duckstar.repository.AdminActionLog.AdminActionLogRepository;
import com.duckstar.repository.Week.WeekRepository;
import com.duckstar.repository.WeekVoteSubmission.WeekVoteSubmissionRepository;
import com.duckstar.security.repository.MemberRepository;
import com.duckstar.security.repository.ShadowBanRepository;
import com.duckstar.web.dto.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.duckstar.web.dto.admin.SubmissionResponseDto.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubmissionService {

    private final WeekVoteSubmissionRepository submissionRepository;
    private final VoteCommandService voteCommandService;
    private final ShadowBanRepository shadowBanRepository;
    private final AdminActionLogRepository adminActionLogRepository;
    private final MemberRepository memberRepository;
    private final WeekRepository weekRepository;

    public SubmissionCountSliceDto getSubmissionCountGroupByIp(Pageable pageable) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();

        Pageable overFetch = PageRequest.of(
                page,
                size + 1,
                pageable.getSort()
        );

        List<SubmissionCountDto> rows =
                submissionRepository.getSubmissionCountDtos(overFetch);

        boolean hasNext = rows.size() > size;
        if (hasNext) rows = rows.subList(0, size);

        PageInfo pageInfo = PageInfo.builder()
                .hasNext(hasNext)
                .page(page)
                .size(size)
                .build();

        return SubmissionCountSliceDto.builder()
                .submissionCountDtos(rows)
                .pageInfo(pageInfo)
                .build();
    }

    @Transactional
    public void withdrawSubmissions(
            Long memberId,
            Long weekId,
            String ipHash,
            String reason
    ) {
        List<WeekVoteSubmission> submissions = submissionRepository.findByWeek_IdAndIpHash(weekId, ipHash);

        submissions.forEach(submission -> submission.setBlocked(true));

        shadowBanRepository.findByIpHash(ipHash).get().setAllWithdrawn(true);

        voteCommandService.refreshEpisodeStatsByWeekId(weekId);

        // 로그 남기기
        adminActionLogRepository.save(
                AdminActionLog.builder()
                        .member(
                                memberRepository.findById(memberId).orElseThrow(() ->
                                        new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND))
                        )
                        .week(
                                weekRepository.findWeekById(weekId).orElseThrow(() ->
                                        new WeekHandler(ErrorStatus.WEEK_NOT_FOUND))
                        )
                        .adminTaskType(AdminTaskType.WITHDRAW)
                        .targetIpHash(ipHash)
                        .reason(reason)
                        .isUndoable(true)
                        .build()
        );
    }

    @Transactional
    public void undoWithdrawnSubmissions(
            Long memberId,
            Long logId,
            Long weekId,
            String ipHash,
            String reason
    ) {
        List<WeekVoteSubmission> submissions = submissionRepository.findByWeek_IdAndIpHash(weekId, ipHash);

        submissions.forEach(submission -> submission.setBlocked(false));

        shadowBanRepository.findByIpHash(ipHash).orElseThrow(() -> new AdminHandler(ErrorStatus.BAN_NOT_FOUND))
                .setAllWithdrawn(false);

        voteCommandService.refreshEpisodeStatsByWeekId(weekId);

        // 롤백 활성화 끄기
        adminActionLogRepository.findById(logId).orElseThrow(() -> new AdminHandler(ErrorStatus.LOG_NOT_FOUND))
                .setIsUndoable(false);

        // 로그 남기기
        adminActionLogRepository.save(
                AdminActionLog.builder()
                        .member(
                                memberRepository.findById(memberId).orElseThrow(() ->
                                        new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND))
                        )
                        .week(
                                weekRepository.findWeekById(weekId).orElseThrow(() ->
                                        new WeekHandler(ErrorStatus.WEEK_NOT_FOUND))
                        )
                        .adminTaskType(AdminTaskType.UNDO_WITHDRAW)
                        .targetIpHash(ipHash)
                        .reason(reason)
                        .isUndoable(false)
                        .build()
        );
    }
}
