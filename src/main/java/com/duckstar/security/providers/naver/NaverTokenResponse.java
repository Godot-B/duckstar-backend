package com.duckstar.security.providers.naver;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NaverTokenResponse {
    String access_token;
    String token_type;
    String expires_in;
    String error;
    String error_description;
}
