package com.duckstar.security.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.MemberHandler;
import com.duckstar.domain.enums.AdminTaskType;
import com.duckstar.domain.mapping.AdminActionLog;
import com.duckstar.repository.AdminActionLog.AdminActionLogRepository;
import com.duckstar.security.domain.ShadowBan;
import com.duckstar.security.repository.MemberRepository;
import com.duckstar.security.repository.ShadowBanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShadowBanService {

    private final ShadowBanRepository shadowBanRepository;
    private final AdminActionLogRepository adminActionLogRepository;
    private final MemberRepository memberRepository;

    public boolean isBanned(String ipHash) {
        return shadowBanRepository.findByIpHash(ipHash)
                .map(ShadowBan::getBanned)
                .orElse(false);
    }

    @Transactional
    public void setBanned(
            Long memberId,
            String ipHash,
            boolean banned,
            String reason
    ) {
        ShadowBan ban = shadowBanRepository.findByIpHash(ipHash).orElseGet(() ->
                shadowBanRepository.save(ShadowBan.create(ipHash)));

        ban.setBanned(banned);

        // 로그 남기기
        adminActionLogRepository.save(
                AdminActionLog.builder()
                        .member(
                                memberRepository.findById(memberId).orElseThrow(() ->
                                        new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND))
                        )
                        .adminTaskType(banned ? AdminTaskType.BAN : AdminTaskType.UNBAN)
                        .targetIpHash(ipHash)
                        .reason(reason)
                        .build()
        );
    }
}