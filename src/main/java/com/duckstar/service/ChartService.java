package com.duckstar.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.WeekHandler;
import com.duckstar.domain.Week;
import com.duckstar.domain.enums.Gender;
import com.duckstar.domain.mapping.Episode;
import com.duckstar.domain.mapping.EpisodeStar;
import com.duckstar.domain.mapping.legacy_vote.AnimeCandidate;
import com.duckstar.domain.mapping.legacy_vote.AnimeVote;
import com.duckstar.domain.vo.RankInfo;
import com.duckstar.repository.AnimeCandidate.AnimeCandidateRepository;
import com.duckstar.repository.AnimeVote.AnimeVoteRepository;
import com.duckstar.repository.Episode.EpisodeRepository;
import com.duckstar.repository.EpisodeStar.EpisodeStarRepository;
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

    private final AnimeCandidateRepository animeCandidateRepository;
    private final AnimeVoteRepository animeVoteRepository;
    private final WeekRepository weekRepository;
    private final EpisodeRepository episodeRepository;
    private final EpisodeStarRepository episodeStarRepository;

    @Transactional
    public void buildDuckstars(LocalDateTime lastWeekEndAt, Long lastWeekId, Long secondLastWeekId) {
        Week lastWeek = weekRepository.findWeekById(lastWeekId).orElseThrow(() ->
                new WeekHandler(ErrorStatus.WEEK_NOT_FOUND));

        //=== 투표 집계, 통계 필드 업데이트 ===//
        List<Episode> episodes = episodeRepository
                        .findAllByScheduledAtGreaterThanEqualAndScheduledAtLessThan(
                                lastWeek.getStartDateTime(), lastWeek.getEndDateTime());

        List<EpisodeStar> allEpisodeStars = episodeStarRepository.findAllByWeekId(lastWeekId);

        Map<Long, List<EpisodeStar>> episodeStarMap = allEpisodeStars.stream()
                .collect(Collectors.groupingBy(es -> es.getEpisode().getId()));

        double weightedSum = 0.0;
        List<Integer> voterCountList = new ArrayList<>();
        for (Episode episode : episodes) {
            List<EpisodeStar> episodeStars = episodeStarMap.get(episode.getId());
            if (episodeStars == null || episodeStars.isEmpty()) {
                int voterCount = episode.getVoterCount();
                voterCountList.add(voterCount);
                weightedSum += episode.getStarAverage() * voterCount;

            } else {
                int voterCount = episodeStars.size();
                voterCountList.add(voterCount);

                int[] scores = new int[10];
                for (EpisodeStar episodeStar : episodeStars) {
                    Integer starScore = episodeStar.getStarScore();
                    int idx = starScore - 1;
                    scores[idx] += 1;
                }

                episode.setStats(voterCount, scores);
                weightedSum += episode.getStarAverage() * voterCount;
            }
        }
        int totalVotes = voterCountList.stream().mapToInt(Integer::intValue).sum();

        voterCountList.sort(Comparator.naturalOrder());
        int size = voterCountList.size();
        int m = (size % 2 == 1)
                ? voterCountList.get(size / 2)
                : (voterCountList.get(size / 2 - 1) + voterCountList.get(size / 2)) / 2;

        double C = totalVotes == 0 ? 0.0 : weightedSum / totalVotes;

        int voterCount = (int) allEpisodeStars.stream()
                .map(es -> es.getWeekVoteSubmission().getId())
                .distinct()
                .count();
        lastWeek.updateAnimeVotes(totalVotes, voterCount);

        //=== 정렬 및 차트 만들기 ===//
        Map<Integer, List<Episode>> chart = buildChart(episodes, m, C);

        //=== 지난 순위와 결합, RankInfo 셋팅 ===//
        Week secondLastWeek = weekRepository.findWeekById(secondLastWeekId).orElse(null);

        Map<Long, RankInfo> lastRankInfoMap = Map.of();
        if (secondLastWeek != null) {
            List<Episode> lastEpisodes = episodeRepository
                    .findAllByScheduledAtGreaterThanEqualAndScheduledAtLessThan(
                    secondLastWeek.getStartDateTime(), secondLastWeek.getEndDateTime());

            if (lastEpisodes != null && !lastEpisodes.isEmpty()) {
                lastRankInfoMap = lastEpisodes.stream()
                        .filter(e -> e.getRankInfo() != null)
                        .collect(Collectors.toMap(
                                e -> e.getAnime().getId(),
                                Episode::getRankInfo
                        ));
            }
        }

        for (Map.Entry<Integer, List<Episode>> entry : chart.entrySet()) {
            int rank = entry.getKey();
            for (Episode episode : entry.getValue()) {  // 동점자 각각 처리
                Long animeId = episode.getAnime().getId();
                RankInfo lastRankInfo = lastRankInfoMap.get(animeId);

                RankInfo rankInfo = RankInfo.create(
                        episode.getStarAverage(),
                        episode.getVoterCount(),
                        lastRankInfo,
                        lastWeekEndAt.toLocalDate(),
                        rank
                );

                episode.setRankInfo(lastWeek, lastRankInfo, rankInfo);
            }
        }
    }

    private Map<Integer, List<Episode>> buildChart(List<Episode> episodes, int m, double C) {
        for (Episode episode : episodes) {
            episode.calculateBayesScore(m, C);
        }

        episodes.sort(Comparator.comparing(Episode::getBayesScore).reversed()  // 베이지안 정렬
                .thenComparing(Episode::getVoterCount, Comparator.reverseOrder())  // 투표자 수 정렬
                .thenComparing(ac -> ac.getAnime().getTitleKor()));  // 가나다 순

        Map<Integer, List<Episode>> chart = new LinkedHashMap<>();
        int rank = 1;
        double prevScore = -1.0;
        for (int i = 0; i < episodes.size(); i++) {
            Episode episode = episodes.get(i);
            double bayesScore = episode.getBayesScore();

            // double 타입 안전한 비교 = epsilon 비교
            if (Math.abs(prevScore - bayesScore) > 1e-9) {
                List<Episode> sameRankEpisodes = new ArrayList<>();
                sameRankEpisodes.add(episode);
                rank = i + 1;
                chart.put(rank, sameRankEpisodes);
            } else {
                chart.get(rank).add(episode);  // 동일 순위 처리
            }

            prevScore = bayesScore;
        }

        return chart;
    }

    @Transactional
    public void buildDuckstars_legacy(LocalDateTime lastWeekEndAt, Long lastWeekId, Long secondLastWeekId) {
        Week lastWeek = weekRepository.findWeekById(lastWeekId).orElseThrow(() ->
                new WeekHandler(ErrorStatus.WEEK_NOT_FOUND));

        //=== 투표 집계, 금주의 덕스타 결정 ===//
        List<AnimeCandidate> candidates = animeCandidateRepository.findAllByWeek_Id(lastWeekId);

        List<AnimeVote> allAnimeVotes = animeVoteRepository.findAllByWeekId(lastWeekId);

        Map<Long, List<AnimeVote>> animeVoteMap = allAnimeVotes.stream()
                .collect(Collectors.groupingBy(v -> v.getAnimeCandidate().getId()));

        int totalVotes = 0;
        for (AnimeCandidate candidate : candidates) {
            List<AnimeVote> animeVotes = animeVoteMap.get(candidate.getId());
            if (animeVotes == null || animeVotes.isEmpty()) {
                continue;
            }

            int score = 0;
            int femaleCount = 0;
            for (AnimeVote animeVote : animeVotes) {
                if (animeVote.getWeekVoteSubmission().getGender() == Gender.FEMALE) femaleCount += 1;
                score += animeVote.getScore();
            }
            int votes = score / 100;
            candidate.updateInfo(votes, animeVotes.size(), femaleCount);

            totalVotes += votes;
        }

        int voterCount = (int) allAnimeVotes.stream()
                .map(av -> av.getWeekVoteSubmission().getId())
                .distinct()
                .count();
        lastWeek.updateAnimeVotes(totalVotes, voterCount);

        //=== 정렬 및 차트 만들기 ===//
        Map<Integer, List<AnimeCandidate>> chart = buildChart_legacy(candidates);

        //=== 지난 순위와 결합, RankInfo 셋팅 ===//
        Week secondLastWeek = weekRepository.findWeekById(secondLastWeekId).orElse(null);

        Map<Long, com.duckstar.domain.mapping.legacy_vote.RankInfo> lastRankInfoMap = Map.of();
        if (secondLastWeek != null) {
            List<AnimeCandidate> lastCandidates = animeCandidateRepository.findAllByWeek_Id(secondLastWeek.getId());

            if (lastCandidates != null && !lastCandidates.isEmpty()) {
                lastRankInfoMap = lastCandidates.stream()
                        .collect(Collectors.toMap(
                                ac -> ac.getAnime().getId(),
                                AnimeCandidate::getRankInfo
                        ));
            }
        }

        for (Map.Entry<Integer, List<AnimeCandidate>> entry : chart.entrySet()) {
            int rank = entry.getKey();
            for (AnimeCandidate candidate : entry.getValue()) {  // 동점자 각각 처리
                double votePercent = totalVotes != 0 ?
                        ((double) candidate.getVotes() / totalVotes) * 100 :
                        0;

                int votes = candidate.getVotes();
                Double malePercent = votes != 0 ?  // 보너스 투표 하나만 있는 경우, 성비 제공 X
                        ((double) candidate.getMaleCount() / candidate.getVoterCount()) * 100 :
                        null;

                Long animeId = candidate.getAnime().getId();
                com.duckstar.domain.mapping.legacy_vote.RankInfo lastRankInfo = lastRankInfoMap.get(animeId);

                com.duckstar.domain.mapping.legacy_vote.RankInfo rankInfo = com.duckstar.domain.mapping.legacy_vote.RankInfo.create(
                        lastRankInfo,
                        lastWeekEndAt.toLocalDate(),
                        rank,
                        votePercent,
                        malePercent
                );

                candidate.setRankInfo(lastRankInfo, rankInfo);
            }
        }
    }

    private Map<Integer, List<AnimeCandidate>> buildChart_legacy(List<AnimeCandidate> candidates) {
        candidates.sort(Comparator.comparing(AnimeCandidate::getVotes).reversed()  // 투표 수 정렬
                .thenComparing(AnimeCandidate::getVoterCount, Comparator.reverseOrder())  // 투표자 수 정렬
                .thenComparing(ac -> ac.getAnime().getTitleKor()));  // 가나다 순

        Map<Integer, List<AnimeCandidate>> chart = new LinkedHashMap<>();
        int rank = 1;
        int prevVotes = -1;
        for (int i = 0; i < candidates.size(); i++) {
            AnimeCandidate candidate = candidates.get(i);
            int votes = candidate.getVotes();

            if (prevVotes != votes) {
                List<AnimeCandidate> animeCandidates = new ArrayList<>();
                animeCandidates.add(candidate);
                rank = i + 1;
                chart.put(rank, animeCandidates);
            } else {
                chart.get(rank).add(candidate);  // 동일 순위 처리
            }

            prevVotes = votes;
        }

        return chart;
    }
}
