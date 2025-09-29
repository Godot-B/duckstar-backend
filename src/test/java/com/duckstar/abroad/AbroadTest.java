package com.duckstar.abroad;

import com.duckstar.abroad.aniLab.Anilab;
import com.duckstar.abroad.aniLab.AnilabRepository;
import com.duckstar.abroad.animeTrend.AnimeTrending;
import com.duckstar.abroad.animeTrend.AnimeTrendingRepository;
import com.duckstar.domain.Anime;
import com.duckstar.domain.HomeBanner;
import com.duckstar.domain.Week;
import com.duckstar.domain.enums.BannerType;
import com.duckstar.repository.HomeBannerRepository;
import com.duckstar.repository.Week.WeekRepository;
import com.duckstar.service.WeekService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@SpringBootTest
@Disabled("로컬 개발용 테스트")
@ActiveProfiles("test-db")
public class AbroadTest {

    @Autowired
    private AnimeTrendingRepository animeTrendingRepository;
    @Autowired
    private WeekService weekService;
    @Autowired
    private AnilabRepository anilabRepository;
    @Autowired
    private WeekRepository weekRepository;
    @Autowired
    private HomeBannerRepository homeBannerRepository;

    @Test
    @Transactional
    @Rollback(false)
    public void testAnimeTrending() throws Exception {
        Long weekId = weekService.getWeekIdByYQW(2025, 3, 12);
        Week week = weekRepository.findWeekById(weekId).get();
        List<AnimeTrending> animeTrendings = new java.util.ArrayList<>(animeTrendingRepository.findAllByWeek_Id(weekId)
                .stream()
                .filter(a -> a.getRankDiff() >= 2 && a.getRank() <= 19)
                .toList());

        animeTrendings.sort(Comparator.comparing(AnimeTrending::getRankDiff).reversed()
                .thenComparing(AnimeTrending::getRank));

        for (AnimeTrending animeTrending : animeTrendings) {
            System.out.println("rank: " + animeTrending.getRank() +
                    " title: " + animeTrending.getTitle() + " rankDiff: " + animeTrending.getRankDiff());
        }

        //=== 배너 생성 ===//
        int bannerNumber = 1;
        LocalDateTime endDateTime = week.getEndDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d");
        String formatted = endDateTime.format(formatter);

        for (AnimeTrending at : animeTrendings) {
            Anime anime = at.getAnime();
            if (anime != null) {
                homeBannerRepository.save(HomeBanner.createByAnime(
                                week,
                                bannerNumber,
                                BannerType.HOT,
                                anime,
                                "Anime Trending, " + formatted + " 기준"
                        )
                );
            }
            bannerNumber += 1;
        }
    }

    @Test
    @Transactional
    @Rollback(false)
    public void testAnilab() throws Exception {
        Long weekId = weekService.getWeekIdByYQW(2025, 3, 13);
        Week week = weekRepository.findWeekById(weekId).get();
        List<Anilab> anilabs = new java.util.ArrayList<>(anilabRepository.findAllByWeek_Id(weekId)
                .stream()
                .filter(a -> a.getRankDiff() >= 2 && a.getRank() <= 15)
                .toList());

        anilabs.sort(Comparator.comparing(Anilab::getRankDiff).reversed()
                .thenComparing(Anilab::getRank));

        for (Anilab anilab : anilabs) {
            System.out.println("rank: " + anilab.getRank() +
                    " title: " + anilab.getTitle() + " rankDiff: " + anilab.getRankDiff());
        }

//        //=== 배너 생성 ===//
//        int bannerNumber = 6;
//        LocalDateTime endDateTime = week.getEndDateTime();
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d");
//        String formatted = endDateTime.format(formatter);
//
//        for (int i = 4; i < 5; i++) {
//            Anime anime = anilabs.get(i).getAnime();
//            if (anime != null) {
//                homeBannerRepository.save(HomeBanner.createByAnime(
//                                week,
//                                bannerNumber,
//                                BannerType.HOT,
//                                anime,
//                                "Anilab, " + formatted + " 기준"
//                        )
//                );
//            }
//            bannerNumber += 1;
//        }
    }
}
