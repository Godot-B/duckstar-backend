package com.duckstar.service;

import com.duckstar.domain.Anime;
import com.duckstar.domain.Season;
import com.duckstar.domain.Week;
import com.duckstar.domain.mapping.Episode;
import com.duckstar.repository.AnimeRepository;
import com.duckstar.repository.Episode.EpisodeRepository;
import com.duckstar.repository.SeasonRepository;
import com.duckstar.repository.Week.WeekRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Disabled("로컬 개발용 테스트")
@ActiveProfiles("test-db")
public class AnimeServiceTest {

    @Autowired AnimeService animeService;
    @Autowired
    private SeasonRepository seasonRepository;
    @Autowired
    private EpisodeRepository episodeRepository;
    @Autowired
    private WeekRepository weekRepository;
    @Autowired
    private AnimeRepository animeRepository;

    @Test
    @Transactional
    public void testCandidates() {
        Season season = seasonRepository.findById(2L).get();
        List<Anime> animes = animeService.getAnimesForCandidate(
                season, LocalDateTime.of(2025, 10, 3, 19, 0)
        );

        System.out.println("후보 수: " + animes.size());
        for (Anime anime : animes) {
            System.out.println("animeTitle: " + anime.getTitleKor() + " 첫 방영일: " + anime.getPremiereDateTime());
        }
    }

    @Test
    @Transactional
    public void testEpisodes() {
        Week week = weekRepository.findById(20L).get();

        List<Episode> lastEpisodes = episodeRepository
                .findAllByScheduledAtGreaterThanEqualAndScheduledAtLessThan(
                        week.getStartDateTime(), week.getEndDateTime());

        List<String> episodesStrings = lastEpisodes.stream()
                .map(e -> {
                    Anime anime = e.getAnime();
                    return "anime_id= " + anime.getId() + " animeTitle: " + anime.getTitleKor() + " 방영 시간: " + e.getScheduledAt();
                })
                .toList();

        System.out.println("후보 수: " + lastEpisodes.size());
        for (String episode : episodesStrings) {
            System.out.println(episode);
        }
    }

//    @Test
//    @Transactional
////    @Rollback(false)
//    public void postEpisodes() {
//        Anime anime = animeRepository.findById(133L).get();
//        int totalEpisodes = anime.getTotalEpisodes();
//
//        //=== 에피소드 생성 ===//
//        List<Episode> episodes = new ArrayList<>();
//        LocalDateTime scheduledAt = anime.getPremiereDateTime();
//        for (int i = 0; i < totalEpisodes; i++) {
//            LocalDateTime nextEpScheduledAt = scheduledAt.plusWeeks(1);
//            episodes.add(Episode.create(
//                    anime,
//                    i + 1,
//                    scheduledAt,
//                    nextEpScheduledAt
//            ));
//            scheduledAt = nextEpScheduledAt;
//        }
//        episodeRepository.saveAll(episodes);
//    }
}
