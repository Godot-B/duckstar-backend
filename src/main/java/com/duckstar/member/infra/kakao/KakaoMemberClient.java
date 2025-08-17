package com.duckstar.member.infra.kakao;

import com.duckstar.member.domain.Member;
import com.duckstar.member.domain.OauthServerType;
import com.duckstar.member.domain.client.MemberClient;
import com.duckstar.member.infra.kakao.client.KakaoApiClient;
import com.duckstar.member.infra.kakao.dto.KakaoMemberReponse;
import com.duckstar.member.infra.kakao.dto.KakaoToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
@RequiredArgsConstructor
public class KakaoMemberClient implements MemberClient {
    private final KakaoApiClient kakaoApiClient;
    private final KakaoOauthConfig kakaoOauthConfig;

    @Override
    public OauthServerType supportServer() {
        return OauthServerType.KAKAO;
    }

    @Override
    public Member fetch(String authCode) {
        // 1. Auth Code를 통해 Access Token을 조회
        KakaoToken tokenInfo = kakaoApiClient.fetchToken(tokenRequestParams(authCode));

        // 2. Access Token을 통해 회원 정보를 받아옴
        KakaoMemberReponse kakaoMemberReponse =
                kakaoApiClient.fetchMember("Bearer " + tokenInfo.accessToken());

        // 3. 회원 정보를 Member 객체로 변환
        return kakaoMemberReponse.toDomain();
    }

    private MultiValueMap<String, String> tokenRequestParams(String authCode) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoOauthConfig.clientId());
        params.add("redirect_uri", kakaoOauthConfig.redirectUrI());
        params.add("code", authCode);
        params.add("client_secret", kakaoOauthConfig.clientSecret());
        return params;
    }
}
