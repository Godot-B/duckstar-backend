package com.duckstar.repository;

import com.duckstar.domain.HomeBanner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HomeBannerRepository extends JpaRepository<HomeBanner, Long> {
    List<HomeBanner> getHomeBannersByWeekIdOrderByBannerNumberAsc(Long weekId);
}
