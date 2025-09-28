package com.duckstar.security.providers.google;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GoogleTokenResponse {

    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("expires_in")
    private int expiresIn;
    @JsonProperty("refresh_token")
    private String refreshToken;
    @JsonProperty("scope")
    private String scope;
    @JsonProperty("token_type")
    private String tokenType;
    @JsonProperty("id_token")
    private String idToken;
}
