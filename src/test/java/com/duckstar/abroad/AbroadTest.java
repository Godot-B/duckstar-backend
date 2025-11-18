package com.duckstar.abroad;

import com.duckstar.abroad.aniLab.Anilab;
import com.duckstar.abroad.aniLab.AnilabRepository;
import com.duckstar.abroad.animeCorner.AnimeCorner;
import com.duckstar.abroad.animeCorner.AnimeCornerRepository;
import com.duckstar.domain.Week;
import com.duckstar.repository.HomeBannerRepository;
import com.duckstar.repository.Week.WeekRepository;
import com.duckstar.service.WeekService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@SpringBootTest
@Disabled("로컬 개발용 테스트")
@ActiveProfiles("test-db")
public class AbroadTest {

    @Autowired
    private AnimeCornerRepository animeCornerRepository;
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
//    @Rollback(false)
    public void testAnimeCorner() throws Exception {
        Long weekId = weekService.getWeekIdByYQW(2025, 3, 12);
        Week week = weekRepository.findWeekById(weekId).get();
        List<AnimeCorner> animeCorners = new java.util.ArrayList<>(animeCornerRepository.findAllByWeek_Id(weekId)
                .stream()
                .filter(a -> a.getRankDiff() >= 2 && a.getRank() <= 10)
                .toList());

        animeCorners.sort(Comparator.comparing(AnimeCorner::getRankDiff).reversed()
                .thenComparing(AnimeCorner::getRank));

        for (AnimeCorner animeCorner : animeCorners) {
            System.out.println("rank: " + animeCorner.getRank() +
                    " title: " + animeCorner.getTitle() + " rankDiff: " + animeCorner.getRankDiff());
        }
    }

    @Test
    @Transactional
//    @Rollback(false)
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
