package com.duckstar.member.domain.client;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.AuthHandler;
import com.duckstar.member.domain.Member;
import com.duckstar.member.domain.OauthServerType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Component
public class MemberClientComposite {

    private final Map<OauthServerType, MemberClient> mapping;

    public MemberClientComposite(Set<MemberClient> clients) {
        mapping = clients.stream()
                .collect(toMap(
                        MemberClient::supportServer,
                        identity()
                ));
    }

    public Member fetch(OauthServerType oauthServerType, String authCode) {
        return getClient(oauthServerType).fetch(authCode);
    }

    private MemberClient getClient(OauthServerType oauthServerType) {
        return Optional.ofNullable(mapping.get(oauthServerType))
                .orElseThrow(() -> new AuthHandler(ErrorStatus.UNSUPPORTED_OAUTH_TYPE));
    }
}
