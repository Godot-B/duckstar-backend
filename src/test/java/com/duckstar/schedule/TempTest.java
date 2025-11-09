package com.duckstar.schedule;

import com.duckstar.domain.Anime;
import com.duckstar.domain.Week;
import com.duckstar.domain.mapping.Episode;
import com.duckstar.domain.vo.RankInfo;
import com.duckstar.repository.AnimeRepository;
import com.duckstar.repository.Episode.EpisodeRepository;
import com.duckstar.repository.Week.WeekRepository;
import com.duckstar.service.ChartService;
import com.duckstar.temp.Temp;
import com.duckstar.temp.TempRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@SpringBootTest
@Disabled("로컬 개발용 테스트")
@ActiveProfiles("test-db")
public class TempTest {

    @Autowired
    private WeekRepository weekRepository;
    @Autowired
    private EpisodeRepository episodeRepository;
    @Autowired
    private TempRepository tempRepository;
    @Autowired
    private ChartService chartService;
    @Autowired
    private AnimeRepository animeRepository;

    @Test
    @Transactional
    @Rollback(false)
    public void calculateRankManual() {
        Week week = weekRepository.findById(24L).get();

        chartService.buildDuckstars(week.getEndDateTime(), 24L);

        chartService.createBanners(24L);
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


        Week week3 = weekRepository.findById(22L).get();

        List<Episode> week3Episodes = episodeRepository
                .findAllByScheduledAtGreaterThanEqualAndScheduledAtLessThan(
                        week3.getStartDateTime(), week3.getEndDateTime()).stream()
                .filter(e -> e.getRankInfo() != null && e.getRankInfo().getRank() != null)
                .toList();
        week3Episodes.forEach(episode -> episode.setRankInfo(null, null));

        chartService.buildDuckstars(week3.getEndDateTime(), 22L);



        Week week4 = weekRepository.findById(23L).get();

        List<Episode> week4Episodes = episodeRepository
                .findAllByScheduledAtGreaterThanEqualAndScheduledAtLessThan(
                        week4.getStartDateTime(), week4.getEndDateTime()).stream()
                .filter(e -> e.getRankInfo() != null && e.getRankInfo().getRank() != null)
                .toList();
        week4Episodes.forEach(episode -> episode.setRankInfo(null, null));

        chartService.buildDuckstars(week4.getEndDateTime(), 23L);
    }

    @Test
    @Transactional
    @Rollback(false)
    public void breakEpisode() {
        // 관리자 화면, path variable 로 프론트에게 받는 id
        Episode brokenEp = episodeRepository.findById(856L).get();
        //=== 휴방으로 셋팅 ===//
        brokenEp.setIsBreak(true);

        LocalDateTime scheduledAt = brokenEp.getScheduledAt();
        Anime anime = brokenEp.getAnime();
        List<Episode> episodes = episodeRepository
                .findAllByAnime_IdOrderByScheduledAtAsc(anime.getId());

        int idx = 0;
        for (Episode episode : episodes) {
            if (episode.getScheduledAt().equals(scheduledAt)) break;
            idx += 1;
        }

        /**
         * ⚠️ 순위 발표 이후 휴방이 발견된 Case
         *  복잡해서 순위는 새로 계산이 더 빠를 듯.
         *  귀찮아지므로 이런 Case 없게 미리미리 휴방 파악해야.
         */

        //=== 남은 에피소드 한 주씩 미루기 ===//
        Integer episodeNumber = brokenEp.getEpisodeNumber();
        for (int i = idx + 1; i < episodes.size(); i++) {
            episodes.get(i).setEpisodeNumber(episodeNumber);
            episodeNumber += 1;
        }
        LocalDateTime nextEpScheduledAt = episodes.get(episodes.size() - 1).getNextEpScheduledAt();
        episodeRepository.save(
                Episode.create(
                        anime,
                        episodeNumber,
                        nextEpScheduledAt,
                        nextEpScheduledAt.plusWeeks(1)
                )
        );
    }

    @Test
    @Transactional
    @Rollback(false)
    public void delayEpisode() {
        Episode targetEp = episodeRepository.findById(1050L).get();

        LocalDateTime scheduledAt = targetEp.getScheduledAt();
        Anime anime = targetEp.getAnime();
        List<Episode> episodes = episodeRepository.
                findAllByAnime_IdOrderByScheduledAtAsc(anime.getId());

        int idx = 0;
        for (Episode episode : episodes) {
            if (episode.getScheduledAt().equals(scheduledAt)) break;
            idx += 1;
        }

        //=== target 포함 이후 에피소드들: 연기일 기준으로 한주씩 미루기 ===//
        LocalDate date = LocalDate.of(2025, 12, 2);
        LocalDateTime delayedDate = LocalDateTime.of(date, targetEp.getScheduledAt().toLocalTime());

        // 직전 에피소드 nextEp 스케줄 수정
        episodes.get(idx - 1).setNextEpScheduledAt(delayedDate);
        // target 포함 이후 에피소드들 수정
        for (int i = idx; i < episodes.size(); i++) {
            Episode episode = episodes.get(i);
            episode.setScheduledAt(delayedDate);
            delayedDate = delayedDate.plusWeeks(1);
            episode.setNextEpScheduledAt(delayedDate);
        }
    }
}
