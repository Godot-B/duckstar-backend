package com.duckstar.security.oauth2;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.AuthHandler;
import com.duckstar.domain.Member;
import com.duckstar.security.MemberPrincipal;
import com.duckstar.security.domain.enums.MemberStatus;
import com.duckstar.security.domain.enums.OAuthProvider;
import com.duckstar.security.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 카카오/구글/네이버에서 사용자 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // kakao, google, naver
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 2. providerId 추출 (서비스별 key 다름)
        String providerId = extractProviderId(registrationId, attributes);

        // 3. 닉네임/프로필 등 기본값 추출
        String nickname = extractNickname(registrationId, attributes);
        String profileImageUrl = extractProfileImageUrl(registrationId, attributes);

        // 4. DB 조회
        Member member = findOrCreateMember(
                registrationId,
                providerId,
                nickname,
                profileImageUrl
        );

        // 5. SecurityContext 에 넣을 Principal 리턴
        return MemberPrincipal.of(member);
    }

    private Member findOrCreateMember(String provider, String providerId, String nickname, String profileImageUrl) {
        // enum 으로 변환
        OAuthProvider oauthProvider = OAuthProvider.valueOf(provider.toUpperCase());

        // 이미 존재하는 경우 → 복구 or 리턴
        return memberRepository.findByProviderAndProviderId(oauthProvider, providerId)
                .map(m -> {
                    if (m.getStatus() == MemberStatus.INACTIVE) {
                        m.restore(oauthProvider, providerId, nickname, profileImageUrl);
                    }
                    return m;
                })
                .orElseGet(() -> {
                    // 신규 생성
                    return memberRepository.save(
                            Member.createSocial(
                                    oauthProvider,
                                    providerId,
                                    nickname,
                                    profileImageUrl
                            )
                    );
                });
    }

    private String extractProviderId(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "kakao" -> String.valueOf(attributes.get("id"));
            case "google" -> String.valueOf(attributes.get("sub"));
            case "naver" -> {
                Map<String, Object> response = (Map<String, Object>) attributes.get("response");
                yield String.valueOf(response.get("id"));
            }
            default -> throw new AuthHandler(ErrorStatus.UNSUPPORTED_OAUTH_TYPE);
        };
    }

    private String extractNickname(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "kakao" -> ((Map<String, Object>) attributes.get("properties")).get("nickname").toString();
            case "google" -> (String) attributes.get("name");
            case "naver" -> (String) ((Map<String, Object>) attributes.get("response")).get("nickname");
            default -> "unknown";
        };
    }

    private String extractProfileImageUrl(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "kakao" -> {
                Map<String, Object> props = (Map<String, Object>) attributes.get("properties");
                yield Objects.toString(props.get("profile_image"), null);
            }
            case "google" -> Objects.toString(attributes.get("picture"), null);
            case "naver" -> {
                Map<String, Object> response = (Map<String, Object>) attributes.get("response");
                yield Objects.toString(response.get("profile_image"), null);
            }
            default -> null;
        };
    }
}
