package com.duckstar.member.domain.authcode;

import com.duckstar.member.domain.OauthServerType;

public interface AuthCodeRequestUrlProvider {
    OauthServerType supportServer();

    String provide();
}
