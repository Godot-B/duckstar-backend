package com.duckstar.member.domain.client;

import com.duckstar.member.domain.Member;
import com.duckstar.member.domain.OauthServerType;

public interface MemberClient {
    OauthServerType supportServer();

    Member fetch(String code);
}
