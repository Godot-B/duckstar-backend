package com.duckstar.security.providers.google;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class GoogleTokenRequest {
    String code;
    String client_id;
    String client_secret;
    String redirect_uri;
    String grant_type;
}
