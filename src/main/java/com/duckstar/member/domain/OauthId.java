package com.duckstar.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OauthId {

    @Column(nullable = false)
    private String oauthServerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OauthServerType oauthServer;

    public String oauthServerId() {
        return oauthServerId;
    }

    public OauthServerType oauthServer() {
        return oauthServer;
    }
}
