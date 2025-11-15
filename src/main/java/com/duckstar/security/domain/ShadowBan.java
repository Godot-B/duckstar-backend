package com.duckstar.security.domain;

import com.duckstar.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShadowBan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 64)
    private String ipHash;

    private Boolean banned;  // shadow-ban 상태

    private Boolean isAllWithdrawn = false;

    protected ShadowBan(String ipHash, Boolean banned) {
        this.ipHash = ipHash;
        this.banned = banned;
    }

    public static ShadowBan create(String ipHash) {
        return new ShadowBan(ipHash, false);
    }

    public void setBanned(Boolean banned) {
        this.banned = banned;
    }

    public void setAllWithdrawn(Boolean isAllWithdrawn) {
        this.isAllWithdrawn = isAllWithdrawn;
    }
}
