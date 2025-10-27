//package com.duckstar.schedule;
//
//import com.duckstar.domain.Anime;
//import com.duckstar.domain.Week;
//import com.duckstar.domain.mapping.Episode;
//import com.duckstar.domain.vo.RankInfo;
//import com.duckstar.repository.AnimeRepository;
//import com.duckstar.repository.Episode.EpisodeRepository;
//import com.duckstar.repository.Week.WeekRepository;
//import com.duckstar.service.ChartService;
//import com.duckstar.temp.Temp;
//import com.duckstar.temp.TempRepository;
//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.annotation.Rollback;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@SpringBootTest
////@Disabled("로컬 개발용 테스트")
//@ActiveProfiles("test-db")
//public class TempTest {
//
//    @Autowired
//    private WeekRepository weekRepository;
//    @Autowired
//    private EpisodeRepository episodeRepository;
//    @Autowired
//    private TempRepository tempRepository;
//    @Autowired
//    private ChartService chartService;
//    @Autowired
//    private AnimeRepository animeRepository;
//
//    @Test
//    @Transactional
////    @Rollback(false)
//    public void calculateRankManual() {
//        Week week = weekRepository.findById(23L).get();
//
//        chartService.buildDuckstars(week.getEndDateTime(), 23L);
//
//        chartService.createBanners(23L);
//    }
//
//    @Test
//    @Transactional
////    @Rollback(false)
//    public void organizeWeekCharts() {
//        List<Anime> animes = animeRepository.findAll();
//        animes.forEach(anime -> anime.initRankInfo(null, null));
//
//        Week week1 = weekRepository.findById(20L).get();
//        List<Episode> week1Episodes = episodeRepository
//                .findAllByScheduledAtGreaterThanEqualAndScheduledAtLessThan(
//                        week1.getStartDateTime(), week1.getEndDateTime()).stream()
//                .filter(e -> e.getRankInfo() != null && e.getRankInfo().getRank() != null)
//                .toList();
//
//        week1Episodes.forEach(episode -> {
//            RankInfo rankInfo = episode.getRankInfo();
//            episode.getAnime().initRankInfo(rankInfo.getRank(), week1.getEndDateTime().toLocalDate());
//        });
//
//        Week week2 = weekRepository.findById(21L).get();
//        List<Episode> week2Episodes = episodeRepository
//                .findAllByScheduledAtGreaterThanEqualAndScheduledAtLessThan(
//                        week2.getStartDateTime(), week2.getEndDateTime()).stream()
//                .filter(e -> e.getRankInfo() != null && e.getRankInfo().getRank() != null)
//                .toList();
//
//        week2Episodes.forEach(episode -> {
//            RankInfo rankInfo = episode.getRankInfo();
//            RankInfo newRankInfo = RankInfo.create(
//                    episode.getAnime(),
//                    rankInfo.getRank(),
//                    week2.getEndDateTime().toLocalDate(),
//                    episode.getUiStarAverage(),
//                    episode.getVoterCount()
//            );
//
//            episode.setRankInfo(week2, newRankInfo);
//        });
//
//        Week week = weekRepository.findById(22L).get();
//        chartService.buildDuckstars(week.getEndDateTime(), 22L);
//    }
//}
