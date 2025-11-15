package com.duckstar.security.repository;

import com.duckstar.security.domain.ShadowBan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShadowBanRepository extends JpaRepository<ShadowBan, Long> {
    Optional<ShadowBan> findByIpHash(String ipHash);
}
