package com.duckstar.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.WeekHandler;
import com.duckstar.domain.HomeBanner;
import com.duckstar.domain.Week;
import com.duckstar.domain.enums.BannerType;
import com.duckstar.domain.enums.EpEvaluateState;
import com.duckstar.domain.mapping.weeklyVote.Episode;
import com.duckstar.domain.mapping.weeklyVote.EpisodeStar;
import com.duckstar.domain.vo.RankInfo;
import com.duckstar.repository.Episode.EpisodeRepository;
import com.duckstar.repository.EpisodeStar.EpisodeStarRepository;
import com.duckstar.repository.HomeBannerRepository;
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
    private final WeekService weekService;

    // 0.5(중간 수준) -> 0.1 작아질 수록 평점 가중치 우선됨
    // 0.5 또는 0.3
    private static final double BASE_WEIGHT = 0.2;

//    @Transactional
//    public void buildDuckstars(LocalDateTime lastWeekEndAt, Long lastWeekId, Long secondLastWeekId) {
//        Week lastWeek = weekRepository.findWeekById(lastWeekId).orElseThrow(() ->
//                new WeekHandler(ErrorStatus.WEEK_NOT_FOUND));
//
//        //=== 투표 집계, 통계 필드 업데이트 ===//
//        List<Episode> episodes = episodeRepository
//                        .findAllByScheduledAtGreaterThanEqualAndScheduledAtLessThan(
//                                lastWeek.getStartDateTime(), lastWeek.getEndDateTime());
//
//        List<EpisodeStar> allEpisodeStars = episodeStarRepository.findAllByWeekId(lastWeekId);
//
//        Map<Long, List<EpisodeStar>> episodeStarMap = allEpisodeStars.stream()
//                .collect(Collectors.groupingBy(es -> es.getEpisode().getId()));
//
//        List<Integer> voterCountList = new ArrayList<>();
//        for (Episode episode : episodes) {
//            List<EpisodeStar> episodeStars = episodeStarMap.get(episode.getId());
//            if (episodeStars == null || episodeStars.isEmpty()) {
//                 voterCountList.add(0);
//                // ⚠️ 전환용
//                // (1) 아래를 위로 대체
//                int voterCount = episode.getVoterCount();
//                voterCountList.add(voterCount);
//
//            } else {
//                int voterCount = episodeStars.size();
//                voterCountList.add(voterCount);
//
//                int[] scores = new int[10];
//                for (EpisodeStar episodeStar : episodeStars) {
//                    Integer starScore = episodeStar.getStarScore();
//                    int idx = starScore - 1;
//                    scores[idx] += 1;
//                }
//
//                episode.setStats(voterCount, scores);
//            }
//        }
//        // 전체 투표 수
//        int totalVotes = voterCountList.stream().mapToInt(Integer::intValue).sum();
//
//
//                // ⚠️ 전환용
//                // (2) uniqueVoterCount 하드 코딩
//        // 고유 투표자 수
//        int uniqueVoterCount = 72/*(int) allEpisodeStars.stream()
//                .map(es -> es.getWeekVoteSubmission().getId())
//                .distinct()
//                .count()*/;
//        lastWeek.updateAnimeVotes(totalVotes, uniqueVoterCount);
//        int minVotes = (int) Math.ceil(0.10 * uniqueVoterCount);
//
//        List<Episode> eligible = new ArrayList<>(
//                episodes.stream()
//                        .filter(e -> e.getVoterCount() >= minVotes)
//                        .toList()
//        );
//
//        List<Episode> ineligible = new ArrayList<>(
//                episodes.stream()
//                        .filter(e -> e.getVoterCount() < minVotes && e.getVoterCount() > 0)
//                        .toList()
//        );
//
//        // 가중치 총 합계
//        double weightedSum = eligible.stream().mapToDouble(Episode::getWeightedSum).sum();
//
//        List<Integer> eligibleCountList = voterCountList.stream()
//                .filter(c -> c >= minVotes)
//                .sorted()
//                .toList();
//
//        int eligibleTotalVotes = eligibleCountList.stream().mapToInt(Integer::intValue).sum();
//
//        int eligibleSize = eligible.size();
//
//        int median = eligible.isEmpty() ? 0 :
//                (eligibleSize % 2 == 1)
//                ? eligibleCountList.get(eligibleSize / 2)
//                : (eligibleCountList.get(eligibleSize / 2 - 1) + eligibleCountList.get(eligibleSize / 2)) / 2;
//
//        double C = eligibleTotalVotes == 0 ? 0.0 : weightedSum / eligibleTotalVotes;
//
//        int p75 = computeP75(eligibleCountList);  // 에피소드별 득표수의 75 분위수
//        int mRule = Math.round(0.25f * uniqueVoterCount);
//        int m = Math.max(median, Math.max(p75, mRule));
//
//        m = Math.max(m, 10); // 하한
//        m = Math.min(m, 40); // 상한 (유입 급증 방지)
//
//        System.out.println("test: " + "weightedSum = " + weightedSum + ", C =" + C + ", m =" + m);
//        //=== 정렬 및 차트 만들기 ===//
//        Map<Integer, List<Episode>> chart = buildChart(eligible, ineligible, m, C);
//
//        //=== 지난 순위와 결합, RankInfo 셋팅 ===//
//        Week secondLastWeek = weekRepository.findWeekById(secondLastWeekId).orElse(null);
//
//        Map<Long, RankInfo> lastRankInfoMap = Map.of();
//        if (secondLastWeek != null) {
//            List<Episode> lastEpisodes = episodeRepository
//                    .findAllByScheduledAtGreaterThanEqualAndScheduledAtLessThan(
//                    secondLastWeek.getStartDateTime(), secondLastWeek.getEndDateTime());
//
//            if (lastEpisodes != null && !lastEpisodes.isEmpty()) {
//                lastRankInfoMap = lastEpisodes.stream()
//                        .filter(e -> e.getRankInfo() != null)
//                        .collect(Collectors.toMap(
//                                e -> e.getAnime().getId(),
//                                Episode::getRankInfo
//                        ));
//            }
//        }
//
//        for (Map.Entry<Integer, List<Episode>> entry : chart.entrySet()) {
//            int rank = entry.getKey();
//            for (Episode episode : entry.getValue()) {  // 동점자 각각 처리
//                Long animeId = episode.getAnime().getId();
//                RankInfo lastRankInfo = lastRankInfoMap.get(animeId);
//
//                RankInfo rankInfo = RankInfo.create(
//                        episode.getStarAverage(),
//                        episode.getVoterCount(),
//                        lastRankInfo,
//                        lastWeekEndAt.toLocalDate(),
//                        rank
//                );
//
//                episode.setRankInfo(lastWeek, lastRankInfo, rankInfo);
//            }
//        }
//    }

    @Transactional
    public void calculateRankByYQW(
            Integer year,
            Integer quarter,
            Integer week
    ) {
        Long weekId = weekService.getWeekIdByYQW(year, quarter, week);

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

        //=== 회수된 표 제외 === //
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
                    .filter(e -> e.getRankInfo().getRankDiff() >= 5)
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

//    @Transactional
//    public void buildDuckstars_legacy(LocalDateTime lastWeekEndAt, Long lastWeekId, Long secondLastWeekId) {
//        Week lastWeek = weekRepository.findWeekById(lastWeekId).orElseThrow(() ->
//                new WeekHandler(ErrorStatus.WEEK_NOT_FOUND));
//
//        //=== 투표 집계, 금주의 덕스타 결정 ===//
//        List<AnimeCandidate> candidates = animeCandidateRepository.findAllByWeek_Id(lastWeekId);
//
//        List<AnimeVote> allAnimeVotes = animeVoteRepository.findAllByWeekId(lastWeekId);
//
//        Map<Long, List<AnimeVote>> animeVoteMap = allAnimeVotes.stream()
//                .collect(Collectors.groupingBy(v -> v.getAnimeCandidate().getId()));
//
//        int totalVotes = 0;
//        for (AnimeCandidate candidate : candidates) {
//            List<AnimeVote> animeVotes = animeVoteMap.get(candidate.getId());
//            if (animeVotes == null || animeVotes.isEmpty()) {
//                continue;
//            }
//
//            int score = 0;
//            int femaleCount = 0;
//            for (AnimeVote animeVote : animeVotes) {
//                if (animeVote.getWeekVoteSubmission().getGender() == Gender.FEMALE) femaleCount += 1;
//                score += animeVote.getScore();
//            }
//            int votes = score / 100;
//            candidate.updateInfo(votes, animeVotes.size(), femaleCount);
//
//            totalVotes += votes;
//        }
//
//        int voterCount = (int) allAnimeVotes.stream()
//                .map(av -> av.getWeekVoteSubmission().getId())
//                .distinct()
//                .count();
//        lastWeek.updateAnimeVotes(totalVotes, voterCount);
//
//        //=== 정렬 및 차트 만들기 ===//
//        Map<Integer, List<AnimeCandidate>> chart = buildChart_legacy(candidates);
//
//        //=== 지난 순위와 결합, RankInfo 셋팅 ===//
//        Week secondLastWeek = weekRepository.findWeekById(secondLastWeekId).orElse(null);
//
//        Map<Long, com.duckstar.domain.mapping.legacy_vote.RankInfo> lastRankInfoMap = Map.of();
//        if (secondLastWeek != null) {
//            List<AnimeCandidate> lastCandidates = animeCandidateRepository.findAllByWeek_Id(secondLastWeek.getId());
//
//            if (lastCandidates != null && !lastCandidates.isEmpty()) {
//                lastRankInfoMap = lastCandidates.stream()
//                        .collect(Collectors.toMap(
//                                ac -> ac.getAnime().getId(),
//                                AnimeCandidate::getRankInfo
//                        ));
//            }
//        }
//
//        for (Map.Entry<Integer, List<AnimeCandidate>> entry : chart.entrySet()) {
//            int rank = entry.getKey();
//            for (AnimeCandidate candidate : entry.getValue()) {  // 동점자 각각 처리
//                double votePercent = totalVotes != 0 ?
//                        ((double) candidate.getVotes() / totalVotes) * 100 :
//                        0;
//
//                int votes = candidate.getVotes();
//                Double malePercent = votes != 0 ?  // 보너스 투표 하나만 있는 경우, 성비 제공 X
//                        ((double) candidate.getMaleCount() / candidate.getVoterCount()) * 100 :
//                        null;
//
//                Long animeId = candidate.getAnime().getId();
//                com.duckstar.domain.mapping.legacy_vote.RankInfo lastRankInfo = lastRankInfoMap.get(animeId);
//
//                com.duckstar.domain.mapping.legacy_vote.RankInfo rankInfo = com.duckstar.domain.mapping.legacy_vote.RankInfo.create(
//                        lastRankInfo,
//                        lastWeekEndAt.toLocalDate(),
//                        rank,
//                        votePercent,
//                        malePercent
//                );
//
//                candidate.setRankInfo(lastRankInfo, rankInfo);
//            }
//        }
//    }
//
//    private Map<Integer, List<AnimeCandidate>> buildChart_legacy(List<AnimeCandidate> candidates) {
//        candidates.sort(Comparator.comparing(AnimeCandidate::getVotes).reversed()  // 투표 수 정렬
//                .thenComparing(AnimeCandidate::getVoterCount, Comparator.reverseOrder())  // 투표자 수 정렬
//                .thenComparing(ac -> ac.getAnime().getTitleKor()));  // 가나다 순
//
//        Map<Integer, List<AnimeCandidate>> chart = new LinkedHashMap<>();
//        int rank = 1;
//        int prevVotes = -1;
//        for (int i = 0; i < candidates.size(); i++) {
//            AnimeCandidate candidate = candidates.get(i);
//            int votes = candidate.getVotes();
//
//            if (prevVotes != votes) {
//                List<AnimeCandidate> animeCandidates = new ArrayList<>();
//                animeCandidates.add(candidate);
//                rank = i + 1;
//                chart.put(rank, animeCandidates);
//            } else {
//                chart.get(rank).add(candidate);  // 동일 순위 처리
//            }
//
//            prevVotes = votes;
//        }
//
//        return chart;
//    }
}
