package com.duckstar.service;

import com.duckstar.domain.Anime;
import com.duckstar.domain.Season;
import com.duckstar.repository.SeasonRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
@Disabled("로컬 개발용 테스트")
@ActiveProfiles("test-db")
public class AnimeServiceTest {

    @Autowired AnimeService animeService;
    @Autowired
    private SeasonRepository seasonRepository;

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
}
