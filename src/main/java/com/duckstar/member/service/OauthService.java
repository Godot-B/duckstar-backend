package com.duckstar.member.service;

import com.duckstar.member.domain.Member;
import com.duckstar.member.domain.OauthServerType;
import com.duckstar.member.domain.authcode.AuthCodeRequestUrlProviderComposite;
import com.duckstar.member.domain.client.MemberClientComposite;
import com.duckstar.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OauthService {

    private final AuthCodeRequestUrlProviderComposite authCodeRequestUrlProviderComposite;
    private final MemberClientComposite memberClientComposite;
    private final MemberRepository memberRepository;

    public String getAuthCodeRequestUrl(OauthServerType oauthServerType) {
        return authCodeRequestUrlProviderComposite.provide(oauthServerType);
    }

    public Long login(OauthServerType oauthServerType, String authCode) {
        Member member = memberClientComposite.fetch(oauthServerType, authCode);

        Member savedMember = memberRepository.findByOauthId(member.getOauthId())
                .orElseGet(() ->
                        memberRepository.save(member)
                );

        return savedMember.getId();
    }
}
