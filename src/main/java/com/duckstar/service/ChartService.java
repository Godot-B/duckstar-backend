package com.duckstar.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.WeekHandler;
import com.duckstar.domain.Week;
import com.duckstar.domain.enums.Gender;
import com.duckstar.domain.mapping.AnimeCandidate;
import com.duckstar.domain.mapping.AnimeVote;
import com.duckstar.domain.vo.RankInfo;
import com.duckstar.repository.AnimeCandidate.AnimeCandidateRepository;
import com.duckstar.repository.AnimeVote.AnimeVoteRepository;
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

    @Transactional
    public void buildDuckstars(LocalDateTime lastWeekEndAt, Long lastWeekId, Long secondLastWeekId) {
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
                .map(AnimeVote::getWeekVoteSubmission)
                .distinct()
                .count();
        lastWeek.updateAnimeVotes(totalVotes, voterCount);

        //=== 정렬 및 차트 만들기 ===//
        Map<Integer, List<AnimeCandidate>> chart = buildChart(candidates);

        //=== 지난 순위와 결합, RankInfo 셋팅 ===//
        Week secondLastWeek = weekRepository.findWeekById(secondLastWeekId).orElse(null);

        Map<Long, RankInfo> lastRankInfoMap = Map.of();
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
                RankInfo lastRankInfo = lastRankInfoMap.get(animeId);

                RankInfo rankInfo = RankInfo.create(
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

    private Map<Integer, List<AnimeCandidate>> buildChart(List<AnimeCandidate> candidates) {
        candidates.sort(Comparator.comparing(AnimeCandidate::getVotes).reversed()  // 투표 수 정렬
                .thenComparing(AnimeCandidate::getVoterCount)  // 투표자 수 정렬
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
                chart.put(i + 1, animeCandidates);
                rank = i + 1;
            } else {
                chart.get(rank).add(candidate);  // 동일 순위 처리
            }

            prevVotes = votes;
        }

        return chart;
    }
}
