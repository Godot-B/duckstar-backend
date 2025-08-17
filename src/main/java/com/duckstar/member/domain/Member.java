package com.duckstar.member.domain;

import com.duckstar.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    uniqueConstraints = {
            @UniqueConstraint(
                    name = "oauth_id_unique",
                    columnNames = {
                            "oauth_server_id",
                            "oauth_server"
                    }
            )
    }
)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private OauthId oauthId;

    private String nickname;

    private String profileImageUrl;

    private String cookieId;

    protected Member(
            OauthId oauthId,
            String nickname,
            String profileImageUrl,
            String cookieId
    ) {
        this.oauthId = oauthId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.cookieId = cookieId;
    }

    public static Member create(
            OauthId oauthId,
            String nickname,
            String profileImageUrl
    ) {
        return new Member(
                oauthId,
                nickname,
                profileImageUrl,
                null
        );
    }
}
