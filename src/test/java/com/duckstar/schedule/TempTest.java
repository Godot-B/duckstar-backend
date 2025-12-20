package com.duckstar.schedule;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.WeekHandler;
import com.duckstar.domain.Anime;
import com.duckstar.domain.Survey;
import com.duckstar.domain.Week;
import com.duckstar.domain.mapping.AnimeSeason;
import com.duckstar.domain.mapping.surveyVote.SurveyCandidate;
import com.duckstar.domain.mapping.weeklyVote.Episode;
import com.duckstar.domain.mapping.weeklyVote.EpisodeStar;
import com.duckstar.domain.vo.RankInfo;
import com.duckstar.repository.AnimeRepository;
import com.duckstar.repository.AnimeSeason.AnimeSeasonRepository;
import com.duckstar.repository.Episode.EpisodeRepository;
import com.duckstar.repository.EpisodeStar.EpisodeStarRepository;
import com.duckstar.repository.SurveyCandidate.SurveyCandidateRepository;
import com.duckstar.repository.SurveyRepository;
import com.duckstar.repository.Week.WeekRepository;
import com.duckstar.service.ChartService;
import com.duckstar.service.VoteService.VoteCommandServiceImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@SpringBootTest
//@Disabled("로컬 개발용 테스트")
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
    private EpisodeStarRepository episodeStarRepository;
    @Autowired
    private VoteCommandServiceImpl voteCommandServiceImpl;
    @Autowired
    private AnimeSeasonRepository animeSeasonRepository;
    @Autowired
    private SurveyRepository surveyRepository;
    @Autowired
    private SurveyCandidateRepository surveyCandidateRepository;

    @Test
    @Transactional
    @Rollback(false)
    public void 에피소드_별점_리프레시() {
        Long weekId = 26L;

        voteCommandServiceImpl.refreshEpisodeStatsByWeekId(weekId);
    }

    @Test
    @Transactional
    public void 부정사용자_통계_출력() {
        Long weekId = 27L;

        Week lastWeek = weekRepository.findWeekById(weekId).orElseThrow(() ->
                new WeekHandler(ErrorStatus.WEEK_NOT_FOUND));

        //=== 회수된 표 제외 ===//
        List<EpisodeStar> allEpisodeStars = episodeStarRepository.findAllEligibleByWeekId(weekId);

        Map<Long, List<EpisodeStar>> episodeStarMap = allEpisodeStars.stream()
                .collect(Collectors.groupingBy(es -> es.getEpisode().getId()));

        //=== 이번 주 휴방 아닌 에피소드들 - 표 집계===//
        List<Episode> episodes = episodeRepository
                .findAllByScheduledAtGreaterThanEqualAndScheduledAtLessThan(
                        lastWeek.getStartDateTime(), lastWeek.getEndDateTime())
                .stream()
                .filter(e -> e.getIsBreak() == null || !e.getIsBreak())
                .toList();

        Map<String, Map<String, List<Integer>>> suspiciousMap = new HashMap<>();
        for (Episode episode : episodes) {
            List<EpisodeStar> thisEpisodeStars =
                    episodeStarMap.get(episode.getId());

            if (thisEpisodeStars != null && !thisEpisodeStars.isEmpty())  {
                //=== 같은 ip 에서 같은 점수 4개 이상 준 경우 감지 ===//
                Map<String, List<Integer>> ipHashToScoresMap = thisEpisodeStars.stream()
                        .collect(Collectors.groupingBy(
                                        es -> es.getWeekVoteSubmission().getIpHash(),
                                        Collectors.mapping(EpisodeStar::getStarScore, Collectors.toList())
                                )
                        );

                for (Map.Entry<String, List<Integer>> entry : ipHashToScoresMap.entrySet()) {
                    Map<Integer, Long> scoreCountMap = entry.getValue().stream()
                            .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

                    boolean hasRepeatedScore = scoreCountMap.values().stream()
                            .anyMatch(count -> count >= 4);

                    if (hasRepeatedScore) {
                        suspiciousMap
                                .computeIfAbsent(episode.getAnime().getTitleKor(), k -> new HashMap<>())
                                .put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }

        System.out.println("=== 동일 점수 4회 이상 반복해서 투표한 IP ===");
        AtomicInteger size = new AtomicInteger();
        suspiciousMap.forEach((anime, ipMap) -> {
            System.out.println("애니메이션: " + anime);
            ipMap.forEach((ip, scores) -> {
                        System.out.println("IP Hash: " + ip + ", Scores: " + scores);
                        size.addAndGet(scores.size());
                    }
            );
            System.out.println();
        });
        System.out.println("제외할 표 수: " + size);
    }

    @Test
    @Transactional
    @Rollback(false)
    public void calculateRankManual() {
        chartService.calculateRankByYQW(2025, 4, 8);
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
//    public void breakEpisode() {
//        // 관리자 화면, path variable 로 프론트에게 받는 id
//        Episode brokenEp = episodeRepository.findById(859L).get();
//        //=== 휴방으로 셋팅 ===//
//        brokenEp.setIsBreak(true);
//
//        LocalDateTime scheduledAt = brokenEp.getScheduledAt();
//        Anime anime = brokenEp.getAnime();
//        List<Episode> episodes = episodeRepository
//                .findAllByAnime_IdOrderByScheduledAtAsc(anime.getId());
//
//        int idx = 0;
//        for (Episode episode : episodes) {
//            if (episode.getScheduledAt().equals(scheduledAt)) break;
//            idx += 1;
//        }
//
//        /**
//         * ⚠️ 순위 발표 이후 휴방이 발견된 Case
//         *  복잡해서 순위는 새로 계산이 더 빠를 듯.
//         *  귀찮아지므로 이런 Case 없게 미리미리 휴방 파악해야.
//         */
//
//        //=== 남은 에피소드 한 주씩 미루기 ===//
//        Integer episodeNumber = brokenEp.getEpisodeNumber();
//        for (int i = idx + 1; i < episodes.size(); i++) {
//            episodes.get(i).setEpisodeNumber(episodeNumber);
//            episodeNumber += 1;
//        }
//        LocalDateTime nextEpScheduledAt = episodes.get(episodes.size() - 1).getNextEpScheduledAt();
//        episodeRepository.save(
//                Episode.create(
//                        anime,
//                        episodeNumber,
//                        nextEpScheduledAt,
//                        nextEpScheduledAt.plusWeeks(1)
//                )
//        );
//    }

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

    @Test
    @Transactional
    @Rollback(false)
    public void postSurveyFromSeasonAnimes() {
        Survey survey = surveyRepository.findById(1L).get();

        List<Anime> animes = animeSeasonRepository.findAllBySeason_Id(2L).stream()
                .map(AnimeSeason::getAnime)
                .toList();

        List<SurveyCandidate> candidates = animes.stream()
                .map(anime -> SurveyCandidate.createByAnime(survey, anime))
                .toList();

        surveyCandidateRepository.saveAll(candidates);
    }
}
