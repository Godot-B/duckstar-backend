package com.duckstar.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.MemberHandler;
import com.duckstar.domain.Member;
import com.duckstar.security.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public Member findByIdOrThrow(Long id) {
        return memberRepository.findById(id).orElseThrow(() ->
                new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }
}
