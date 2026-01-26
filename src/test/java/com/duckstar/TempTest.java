package com.duckstar;

import com.duckstar.domain.Anime;
import com.duckstar.domain.Week;
import com.duckstar.domain.mapping.weeklyVote.Episode;
import com.duckstar.domain.vo.RankInfo;
import com.duckstar.repository.*;
import com.duckstar.repository.Episode.EpisodeRepository;
import com.duckstar.repository.Week.WeekRepository;
import com.duckstar.service.ChartService;
import com.duckstar.service.WeekService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@SpringBootTest
@Disabled("로컬 개발용 테스트")
@ActiveProfiles("local-db")
public class TempTest {

    @Autowired
    private WeekRepository weekRepository;
    @Autowired
    private EpisodeRepository episodeRepository;
    @Autowired
    private ChartService chartService;
    @Autowired
    private AnimeRepository animeRepository;
    @Autowired
    private WeekService weekService;

    @Test
    @Transactional
    @Rollback(false)
    public void calculateRankManual() {
        Long weekId = weekService.getWeekIdByYQW(2026, 1, 1);

        chartService.calculateRankByYQW(weekId);
    }

    @Test
    @Transactional
    @Rollback(false)
    public void organizeWeekCharts() {
        List<Anime> animes = animeRepository.findAll();
        animes.forEach(anime -> anime.initRankInfo(null, null));

        Week week1 = weekRepository.findById(20L).get();
        List<Episode> week1Episodes = episodeRepository
                .findAllByScheduledAtGreaterThanEqualAndScheduledAtLessThan(
                        week1.getStartDateTime(), week1.getEndDateTime()).stream()
                .filter(e -> e.getRankInfo() != null && e.getRankInfo().getRank() != null)
                .toList();

        week1Episodes.forEach(episode -> {
            RankInfo rankInfo = episode.getRankInfo();
            episode.getAnime().initRankInfo(rankInfo.getRank(), week1.getEndDateTime().toLocalDate());
        });


        Week week2 = weekRepository.findById(21L).get();
        List<Episode> week2Episodes = episodeRepository
                .findAllByScheduledAtGreaterThanEqualAndScheduledAtLessThan(
                        week2.getStartDateTime(), week2.getEndDateTime()).stream()
                .filter(e -> e.getRankInfo() != null && e.getRankInfo().getRank() != null)
                .toList();

        week2Episodes.forEach(episode -> {
            RankInfo rankInfo = episode.getRankInfo();
            RankInfo newRankInfo = RankInfo.create(
                    episode.getAnime(),
                    rankInfo.getRank(),
                    week2.getEndDateTime().toLocalDate(),
                    episode.getUiStarAverage(),
                    episode.getVoterCount()
            );

            episode.setRankInfo(week2, newRankInfo);
        });

        //=== 덕스타 런칭부터 임의의 주차까지의 week_id들 ===//
        Long[] weekIdList = { 22L, 23L, 24L, 25L, 26L, 27L };

        for (Long weekId : weekIdList) {
            Week week = weekRepository.findById(weekId).get();

            List<Episode> week3Episodes = episodeRepository
                    .findAllByScheduledAtGreaterThanEqualAndScheduledAtLessThan(
                            week.getStartDateTime(), week.getEndDateTime()).stream()
                    .filter(e -> e.getRankInfo() != null && e.getRankInfo().getRank() != null)
                    .toList();
            week3Episodes.forEach(episode -> episode.setRankInfo(null, null));

            chartService.buildDuckstars(weekId, true);
        }
    }

//    @Test
//    @Transactional
//    @Rollback(false)
//    public void 서베이_순위_수동_계산() {
//        // 제외할 IP들
//        // 25년 4분기 결산
////        String[] outlawStrings = {"01fca71789934899520ec2424e670e4ca2558fe8cbc35e8cfb35f472b27e7aa6",
////                "ffdb08139e54e6cea7f7f88b59ca680ef369b1dad848a214b92b422734c98c54",
////                "d0a4a91c903d6ba64c67a0a7aaf2c7242359ae57b651ad7de76b28bbb42deab6",
////                "2208af4b7a1e66d7ae959b7d1a3766c1da2cd3ecf51b381c20fce178541674f4",
////                "26de304d6fdb492845f863822354ddccccee3fd62a00cad400864383b44dafac",
////                "91abc9d217c6c93ccf6d8a6386cb8a8d04ecf89dfa139bf7a69fa74af237da4b"};
//        // 25년 연말 결산
//        String[] outlawStrings = {"01fca71789934899520ec2424e670e4ca2558fe8cbc35e8cfb35f472b27e7aa6",
//                "ffdb08139e54e6cea7f7f88b59ca680ef369b1dad848a214b92b422734c98c54",
//                "c7fee37b082cbcd382d9dd59285ab68303d4c64c7bee5492b5d1bffe044d6973"};
//        // 26년 1분기 기대작 투표
////        String[] outlawStrings = {"71c35dc8a24d615d53bfb259e843693cbdee9ac70e6737e71a17dd7a9debd3f5"};
//
//        chartService.buildSurveyAwards(
//                2L,
//                Arrays.asList(outlawStrings),
//                true
//        );
//    }
}
