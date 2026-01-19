package com.duckstar.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.SurveyHandler;
import com.duckstar.apiPayload.exception.handler.WeekHandler;
import com.duckstar.domain.HomeBanner;
import com.duckstar.domain.Survey;
import com.duckstar.domain.Week;
import com.duckstar.domain.enums.BannerType;
import com.duckstar.domain.enums.EpEvaluateState;
import com.duckstar.domain.enums.SurveyStatus;
import com.duckstar.domain.mapping.surveyVote.SurveyCandidate;
import com.duckstar.domain.mapping.weeklyVote.Episode;
import com.duckstar.domain.mapping.weeklyVote.EpisodeStar;
import com.duckstar.domain.vo.RankInfo;
import com.duckstar.repository.Episode.EpisodeRepository;
import com.duckstar.repository.EpisodeStar.EpisodeStarRepository;
import com.duckstar.repository.HomeBannerRepository;
import com.duckstar.repository.SurveyCandidate.SurveyCandidateRepository;
import com.duckstar.repository.SurveyRepository;
import com.duckstar.repository.SurveyVote.SurveyVoteRepository;
import com.duckstar.repository.SurveyVoteSubmission.SurveyVoteSubmissionRepository;
import com.duckstar.repository.Week.WeekRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChartService {

    private final WeekRepository weekRepository;
    private final EpisodeRepository episodeRepository;
    private final EpisodeStarRepository episodeStarRepository;
    private final HomeBannerRepository homeBannerRepository;

    // 0.5(중간 수준) -> 0.3 으로 작아질 수록 평점 가중치 우선됨
    // ⚠️ 권장: 0.5 또는 0.3
    private static final double BASE_WEIGHT = 0.5;
    private final SurveyRepository surveyRepository;
    private final SurveyCandidateRepository surveyCandidateRepository;
    private final SurveyVoteRepository surveyVoteRepository;
    private final SurveyVoteSubmissionRepository surveyVoteSubmissionRepository;

    @Transactional
    public void calculateRankByYQW(Long weekId) {
        // 차트 계산, 발표 준비 완료
        buildDuckstars(weekId, false);

        // 배너 생성
        createBanners(weekId);
    }

    @Transactional
    public void buildDuckstars(Long lastWeekId, Boolean isForOrganizing) {
        Week lastWeek = weekRepository.findWeekById(lastWeekId).orElseThrow(() ->
                new WeekHandler(ErrorStatus.WEEK_NOT_FOUND));

        if (!isForOrganizing && lastWeek.getAnnouncePrepared()) {
            throw new WeekHandler(ErrorStatus.WEEK_ANNOUNCED_ALREADY);
        }

        //=== 회수된 표 제외 ===//
        // WeekVoteSubmission과 관계지은 EpisodeStar들만 조회
        // ALWAYS_OPEN 인 에피소드는 WeekVoteSubmission의 관계 없이 생성되므로 OK (추후 개발)
        List<EpisodeStar> allEpisodeStars = episodeStarRepository.findAllEligibleByWeekId(lastWeekId);

        Map<Long, List<EpisodeStar>> episodeStarMap = allEpisodeStars.stream()
                .collect(Collectors.groupingBy(es -> es.getEpisode().getId()));

        //=== 이번 주 휴방 아닌 에피소드들 - 표 집계 ===//
        List<Episode> episodes = episodeRepository
                .findAllByScheduledAtGreaterThanEqualAndScheduledAtLessThan(
                        lastWeek.getStartDateTime(), lastWeek.getEndDateTime())
                .stream()
                .filter(e -> !e.isBreak())
                .toList();

        List<Integer> voterCountList = new ArrayList<>();
        for (Episode episode : episodes) {
            List<EpisodeStar> thisEpisodeStars =
                    episodeStarMap.get(episode.getId());

            if (!isForOrganizing
                    // 모든 에피소드가 주차 마감을 기다리는 상태여야 함
                    && episode.getEvaluateState() != EpEvaluateState.LOGIN_REQUIRED) {
                throw new WeekHandler(ErrorStatus.WEEK_NOT_CLOSED);
            }

            if (thisEpisodeStars == null || thisEpisodeStars.isEmpty()) {
                voterCountList.add(0);

            } else {
                //=== 같은 ip 에서 같은 점수 4개 이상 준 경우 감지 ===//
                Map<String, List<Integer>> ipHashScoresMap = thisEpisodeStars.stream()
                        .collect(Collectors.groupingBy(
                                        es -> es.getWeekVoteSubmission().getIpHash(),
                                        Collectors.mapping(EpisodeStar::getStarScore, Collectors.toList())
                                )
                        );

                List<String> blockedIpHashes = new ArrayList<>();

                for (Map.Entry<String, List<Integer>> entry : ipHashScoresMap.entrySet()) {
                    Map<Integer, Long> scoreCountMap = entry.getValue().stream()
                            .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

                    boolean hasRepeatedScore = scoreCountMap.values().stream()
                            .anyMatch(count -> count >= 4);

                    if (hasRepeatedScore) {
                        blockedIpHashes.add(entry.getKey());
                    }
                }

                // 제외시키기
                thisEpisodeStars = thisEpisodeStars.stream()
                        .filter(es -> !blockedIpHashes.contains(es.getWeekVoteSubmission().getIpHash()))
                        .toList();

                int voterCount = thisEpisodeStars.size();
                voterCountList.add(voterCount);

                int[] scores = new int[10];

                for (EpisodeStar episodeStar : thisEpisodeStars) {
                    Integer starScore = episodeStar.getStarScore();
                    int idx = starScore - 1;
                    scores[idx] += 1;
                }

                episode.setStats(voterCount, scores);

                // 투표 마감
                episode.setEvaluateState(EpEvaluateState.ALWAYS_OPEN);
            }
        }

        List<Episode> votedEpisodes = new ArrayList<>(
                episodes.stream()
                        .filter(e -> e.getVoterCount() != null && e.getVoterCount() > 0)
                        .toList()
        );
        List<Integer> votedCountList = new ArrayList<>(
                voterCountList.stream()
                        .filter(c -> c > 0)
                        .sorted()
                        .toList()
        );
        // 전체 투표 수
        int totalVotes = votedCountList.stream().mapToInt(Integer::intValue).sum();

        // 고유 투표자 수
        int uniqueVoterCount = (int) allEpisodeStars.stream()
                    .map(es -> es.getWeekVoteSubmission().getId())
                    .distinct()
                    .count();
        lastWeek.updateAnimeVotes(totalVotes, uniqueVoterCount);
        int minVotes = (int) Math.ceil(0.1 * uniqueVoterCount);

        // 가중치 총 합계
        double weightedSum = votedEpisodes.stream()
                .mapToDouble(Episode::getWeightedSum)
                .sum();  // 전체 합계 10점 만점 스케일로 맞춤

        // === median 리스트 만들기 === //
        List<Integer> medianList = votedEpisodes.stream()
                .map(Episode::getVoterCount)
                .sorted()
                .toList();

        int size = medianList.size();

        int median = (size == 0) ? 0 :
                (size % 2 == 1)
                        ? medianList.get(size / 2)
                        : (medianList.get(size / 2 - 1) + medianList.get(size / 2)) / 2;

        double C = totalVotes == 0 ? 0.0 : weightedSum / totalVotes;

        int p75 = computeP75(votedCountList);  // 에피소드별 득표수의 75 분위수
        int mRule = Math.round(0.25f * uniqueVoterCount);
        int m = Math.max(median, Math.max(p75, mRule));

        m = Math.max(m, 10); // 하한

        int mCap = (int) Math.min(100, Math.round(0.3 * uniqueVoterCount));
        m = Math.min(m, mCap); // 상한 (유입 급증 방지)

        System.out.println("=====투표 정책=====");
        System.out.println("totalVotes = " + totalVotes);
        System.out.println("uniqueVoterCount = " + uniqueVoterCount);
        System.out.println("minVotes = " + minVotes);
        System.out.println("weightedSum = " + weightedSum);
        System.out.println("m = " + m);
        System.out.println("C = " + C);

        //=== 정렬 및 차트 만들기 ===//
        Map<Integer, List<Episode>> chart = buildChart(
                votedEpisodes,
                m,
                C,
                minVotes
        );

        //=== Anime 스트릭과 결합, RankInfo 셋팅 ===//
        for (Map.Entry<Integer, List<Episode>> entry : chart.entrySet()) {
            int rank = entry.getKey();
            LocalDateTime lastWeekEndAt = lastWeek.getEndDateTime();
            for (Episode episode : entry.getValue()) {  // 동점자 각각 처리
                RankInfo rankInfo = RankInfo.create(
                        episode.getAnime(),
                        rank,
                        lastWeekEndAt.toLocalDate(),
                        episode.getUiStarAverage(),
                        episode.getVoterCount()
                );

                episode.setRankInfo(lastWeek, rankInfo);
            }
        }

        // 발표 준비 완료
        lastWeek.setAnnouncePrepared(true);
    }

    private int computeP75(List<Integer> voterCountList) {
        if (voterCountList == null || voterCountList.isEmpty()) {
            return 0; // 안전장치
        }

        List<Integer> sorted = voterCountList.stream()
                .sorted()
                .toList();

        int n = sorted.size();
        double pos = 0.75 * (n - 1);
        int lowerIndex = (int) Math.floor(pos);
        int upperIndex = (int) Math.ceil(pos);

        if (lowerIndex == upperIndex) {
            return sorted.get(lowerIndex);
        } else {
            double lower = sorted.get(lowerIndex);
            double upper = sorted.get(upperIndex);
            double value = lower + (pos - lowerIndex) * (upper - lower);
            return (int) Math.round(value); // 분위수를 정수로 반환
        }
    }

    private Map<Integer, List<Episode>> buildChart(
            List<Episode> episodes,
            int m,
            double C,
            int minVotes
    ) {
        final double kappa = 0.6;
        //=== 베이지안 계산 ===//
        for (Episode episode : episodes) {
            int deficit = Math.max(0, minVotes - episode.getVoterCount());
            int mDynamic = m + (int) (kappa * deficit);
            mDynamic = Math.min(mDynamic, 100);  // 상한

            episode.calculateBayesScore(mDynamic, C);
        }

        //=== episodes 정렬 ===//

        episodes.sort((a, b) -> {
            double bBayes = b.getBayesScore();
            double aBayes = a.getBayesScore();
            double bayesDiff = bBayes - aBayes;  // DESC

            int bV = b.getVoterCount();
            int aV = a.getVoterCount();
            // 베이지안 점수 우선
            if (Math.abs(bayesDiff) >= epsDynamic(aV, bV)) {  // 동적 엡실론 타이브레이커
                return bayesDiff > 0 ? 1 : -1;
            }

            // 점수 차이가 EPS 미만 -> 동점으로 간주
            int byVoterCount = Integer.compare(bV, aV);
            if (byVoterCount != 0) return byVoterCount;

            double averageDiff = b.getUiStarAverage() - a.getUiStarAverage();  // DESC
            if (averageDiff != 0) {
                return averageDiff > 0 ? 1 : -1;
            }
            return 0;
        });

        //=== Competition Ranking (공통 루틴) ===//
        Map<Integer, List<Episode>> chart = new LinkedHashMap<>();
        int processed = 0;                 // 누적 항목 수
        int currentGroupSize = 0;          // 현재 그룹 크기
        int rank = 1;

        //=== episodes 그룹핑 (키: bayesScore + voterCount) ===//
        double prevScore = Double.NaN;
        int prevVoterCount = Integer.MIN_VALUE;
        double prevAverage = Double.NaN;

        for (Episode episode : episodes) {
            double bayesScore = episode.getBayesScore();
            int voterCount = episode.getVoterCount();
            double starAverage = episode.getUiStarAverage();

            boolean newGroup = currentGroupSize == 0
                    || Math.abs(prevScore - bayesScore) > epsDynamic(voterCount, prevVoterCount)
                    || prevAverage != starAverage
                    || prevVoterCount != voterCount;

            if (newGroup) {
                // 이전 그룹 마감 → 누적 반영 & 새 랭크
                processed += currentGroupSize;
                rank = processed + 1;
                chart.put(rank, new ArrayList<>());
                currentGroupSize = 0;
            }
            chart.get(rank).add(episode);  // 동일 순위 처리
            currentGroupSize += 1;

            prevScore = bayesScore;
            prevVoterCount = voterCount;
            prevAverage = starAverage;
        }
        // 마지막 그룹 반영 (마감)
        processed += currentGroupSize;

        // ...

        return chart;
    }

    private double epsDynamic(int a, int b) {
        // 작은 표본 쪽 불확실성 우선 반영
        double base = BASE_WEIGHT * (1/Math.sqrt(a) + 1/Math.sqrt(b));

        // 최소 허용치
        double min = 0.005;
        return Math.max(min, base);
    }

    @Transactional
    public void createBanners(Long lastWeekId) {
        Week lastWeek = weekRepository.findWeekById(lastWeekId).orElseThrow(() ->
                new WeekHandler(ErrorStatus.WEEK_NOT_FOUND));

        List<Episode> episodes = episodeRepository
                .findAllByScheduledAtGreaterThanEqualAndScheduledAtLessThan(
                        lastWeek.getStartDateTime(), lastWeek.getEndDateTime())
                .stream()
                .filter(e -> e.getRankInfo() != null && e.getRankInfo().getRank() != null)
                .sorted(Comparator.comparing(e -> e.getRankInfo().getRank()))
                .toList();

        int remainSize = 6;
        int batchSize = 10;
        int i = 0;
        int number = 1;

        while (remainSize > 0 && i * batchSize < episodes.size()) {
            int from = i * batchSize;
            int to = Math.min(episodes.size(), (i + 1) * batchSize);
            List<Episode> subList = episodes.subList(from, to);

            // 1. rankDiff == null
            List<Episode> newEpisodes = subList.stream()
                    .filter(e -> e.getRankInfo().getRankDiff() == null)
                    .sorted(Comparator.comparing(e -> e.getRankInfo().getRank()))
                    .limit(remainSize)
                    .toList();

            for (Episode episode : newEpisodes) {
                homeBannerRepository.save(
                        HomeBanner.createByAnime(
                                lastWeek,
                                number,
                                BannerType.NOTICEABLE,
                                episode.getAnime()
                        )
                );
                number += 1;
            }
            remainSize = remainSize - newEpisodes.size();

            if (remainSize == 0) break;

            // 2. rankDiff >= 5
            List<Episode> hotEpisodes = subList.stream()
                    .filter(e -> e.getRankInfo().getRankDiff() != null &&
                            e.getRankInfo().getRankDiff() >= 5)
                    .sorted(Comparator.comparing(e -> e.getRankInfo().getRankDiff(), Comparator.reverseOrder()))
                    .limit(remainSize)
                    .toList();

            for (Episode episode : hotEpisodes) {
                homeBannerRepository.save(
                        HomeBanner.createByAnime(
                                lastWeek,
                                number,
                                BannerType.HOT,
                                episode.getAnime()
                        )
                );
                number += 1;
            }
            remainSize = remainSize - hotEpisodes.size();

            i += 1;
        }
    }

    public record SurveyStatRecord(
            Integer score,  // 도장 점수 합
            Long voterCount,  // 투표자 수
            Integer normalCount,  // 1. 표 종류 카운트
            Integer bonusCount,
            Integer maleCount,  // 2. 성별 카운트
            Integer femaleCount,
            Integer under14,  // 3. 연령대별 카운트
            Integer age1519,
            Integer age2024,
            Integer age2529,
            Integer age3034,
            Integer over35
    ) {}

    @Transactional
    public void buildSurveyAwards(
            Long surveyId,
            List<String> outlaws,
            boolean forMidTermTest
    ) {
        // 서베이 존재, 상태 점검
        Survey survey = surveyRepository.findById(surveyId).orElseThrow(() ->
                new SurveyHandler(ErrorStatus.SURVEY_NOT_FOUND));
        if (!forMidTermTest && survey.getStatus() != SurveyStatus.CLOSED) {
            throw new SurveyHandler(ErrorStatus.SURVEY_NOT_CLOSED);
        }

        // 전체 표에서 제외, 통계 맵 구성
        Map<Long, SurveyStatRecord> statMap = surveyVoteRepository
                .getEligibleStatMapBySurveyId(surveyId, outlaws);

        //=== 통계 셋팅 ===//
        int totalVotes = 0;
        List<SurveyCandidate> candidates = surveyCandidateRepository.findAllBySurvey_Id(surveyId);
        for (SurveyCandidate candidate : candidates) {
            SurveyStatRecord record = statMap.get(candidate.getId());
            if (record == null) {
                continue;
            }

            candidate.updateStatistics(record);
            totalVotes = totalVotes + candidate.getVotes();
        }

        //=== 순위 결정 ===//

        // 1. 정렬: 표 수 -> 일반 표 퍼센트 순
        candidates.sort((a, b) -> {
            // 득표 수
            int aVotes = a.getVotes() == null ? 0 : a.getVotes();
            int bVotes = b.getVotes() == null ? 0 : b.getVotes();
            int byVotes = Integer.compare(bVotes, aVotes);
            if (byVotes != 0) return byVotes;

            // 일반 표 퍼센트
            double aPercent = a.getNormalPercent() == null ? 0.0 : a.getNormalPercent();
            double bPercent = b.getNormalPercent() == null ? 0.0 : b.getNormalPercent();

            return Double.compare(bPercent, aPercent);
        });

        // 2. Competition Ranking 부여
        int currentRank = 1;
        for (int i = 0; i < candidates.size(); i++) {
            SurveyCandidate current = candidates.get(i);

            if (i > 0) {
                SurveyCandidate previous = candidates.get(i - 1);

                double cPercent = current.getNormalPercent() == null ? 0.0 : current.getNormalPercent();
                double pPercent = previous.getNormalPercent() == null ? 0.0 : previous.getNormalPercent();
                boolean isTie = current.getVotes().equals(previous.getVotes()) &&
                        Double.compare(cPercent, pPercent) == 0;

                if (!isTie) {
                    currentRank = i + 1;
                }
            }

            double votePercent = totalVotes != 0 ?
                    (current.getVotes() / (double) totalVotes) * 100 :
                    0.0;

            current.setRankAndVotePercent(currentRank, votePercent);
        }

        // 전체 투표자 수
        Long totalVoterCount = surveyVoteSubmissionRepository
                .getEligibleCountBySurveyId(surveyId, outlaws);

        survey.setVotesAndVoterCount(
                totalVotes,
                totalVoterCount == null ? 0 : totalVoterCount.intValue()
        );
    }
}
